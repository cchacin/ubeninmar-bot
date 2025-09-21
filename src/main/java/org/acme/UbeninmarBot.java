package org.acme;

import org.acme.service.LinkProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class UbeninmarBot implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UbeninmarBot.class);

    private final TelegramClient client;
    private final LinkProcessor linkProcessor;

    public UbeninmarBot(TelegramClient client, LinkProcessor linkProcessor) {
        this.client = client;
        this.linkProcessor = linkProcessor;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var messageText = update.getMessage().getText();
            var chatId = update.getMessage().getChatId().toString();

            LOGGER.info("Message received from chat {}: {}", chatId, messageText);

            // Process the message for Amazon links
            var processedLinks = linkProcessor.processMessage(messageText);

            if (!processedLinks.isEmpty()) {
                // Send responses for each successfully processed link
                var responses = linkProcessor.formatResponses(processedLinks);

                for (var response : responses) {
                    sendResponse(chatId, response);
                }

                LOGGER.info("Processed {} Amazon links for chat {}", processedLinks.size(), chatId);
            } else {
                LOGGER.debug("No Amazon links found in message from chat {}", chatId);
            }
        }
    }

    private void sendResponse(String chatId, String responseText) {
        var sendMessage = SendMessage.builder().chatId(chatId).text(responseText).build();

        try {
            client.execute(sendMessage);
            LOGGER.debug("Response sent to chat {}: {}", chatId, responseText);
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to send response to chat {}: {}", chatId, responseText, e);
        }
    }
}
