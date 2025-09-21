package org.acme;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.acme.service.AffiliateService;
import org.acme.service.AmazonLinkService;
import org.acme.service.LinkProcessor;
import org.acme.util.HttpRedirectFollower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        try (var botsApplication = new TelegramBotsLongPollingApplication();
                var httpClient =
                        HttpClient.newBuilder()
                                .followRedirects(HttpClient.Redirect.NEVER)
                                .connectTimeout(Duration.ofSeconds(10))
                                .build()) {
            var botToken = System.getenv("BOT_TOKEN");
            var client = new OkHttpTelegramClient(botToken);
            var amazonLinkService =
                    new AmazonLinkService(
                            new HttpRedirectFollower(
                                            httpRequest -> {
                                                try {
                                                    return httpClient.send(
                                                            httpRequest,
                                                            HttpResponse.BodyHandlers.ofString());
                                                } catch (Exception e) {
                                                    return null;
                                                }
                                            })
                                    ::followRedirects,
                            new AffiliateService(System.getenv("AFFILIATE_TAG"))::addAffiliateTag);
            var linkProcessor =
                    new LinkProcessor(
                            amazonLinkService::isAmazonUrl, amazonLinkService::processAmazonUrl);
            botsApplication.registerBot(
                    botToken,
                    new BeautyByUbeBot(
                            client, linkProcessor::processMessage, linkProcessor::formatResponses));
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("Error instantiating bot", e);
        }
    }
}
