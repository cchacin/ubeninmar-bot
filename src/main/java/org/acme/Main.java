package org.acme;

import org.acme.service.AffiliateService;
import org.acme.service.AmazonLinkService;
import org.acme.service.LinkProcessor;
import org.acme.util.HttpRedirectFollower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    void main() {
        try {
            var botsApplication = new TelegramBotsLongPollingApplication();
            var botToken = System.getenv("BOT_TOKEN");
            var client = new OkHttpTelegramClient(botToken);
            var linkProcessor =
                    new LinkProcessor(
                            new AmazonLinkService(
                                    new HttpRedirectFollower(), new AffiliateService()));
            botsApplication.registerBot(botToken, new UbeninmarBot(client, linkProcessor));
        } catch (TelegramApiException e) {
            LOGGER.error("Error instantiating bot", e);
        }
    }
}
