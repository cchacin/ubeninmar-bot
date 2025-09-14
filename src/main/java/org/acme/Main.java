package org.acme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var botsApplication = new TelegramBotsLongPollingApplication();
        try {
            var botToken = System.getenv("BOT_TOKEN");
            var client = new OkHttpTelegramClient(botToken);
            botsApplication.registerBot(botToken, new UbeninmarBot(client));
        } catch (TelegramApiException e) {
            LOGGER.error("Error instantiating bot", e);
        }
    }
}
