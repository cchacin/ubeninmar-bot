package org.acme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UbeninmarBot implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UbeninmarBot.class);

    private final TelegramClient client;

    // The regex to find the domain (group 1) and ASIN (group 2)
    private static final String AMAZON_REGEX =
            "https?://(?:www\\.)?(amazon|a\\.[a-z.]{2,6}).*?(?:/dp/|/gp/product/)([A-Z0-9]{10}).*";

    // Compile the regex into a Pattern object for efficiency.
    // CASE_INSENSITIVE flag handles domains like "Amazon.com"
    private static final Pattern AMAZON_PATTERN =
            Pattern.compile(AMAZON_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Parses an Amazon URL to extract the domain and ASIN.
     *
     * @param url The URL string to parse.
     */
    public static void parseUrl(String url) {
        if (url == null || url.isEmpty()) {
            System.out.println("URL is empty or null.\n");
            return;
        }

        Matcher matcher = AMAZON_PATTERN.matcher(url);

        // The matches() method checks if the entire string conforms to the pattern
        if (matcher.matches()) {
            String domain = matcher.group(1); // Extract the first capturing group (the domain)
            String asin = matcher.group(2); // Extract the second capturing group (the ASIN)

            System.out.println("✅ Valid Amazon URL: " + url);
            System.out.println("   -> Domain: " + domain);
            System.out.println("   -> ASIN: " + asin + "\n");
        } else {
            System.out.println("❌ Invalid Amazon URL: " + url + "\n");
        }
    }

    public UbeninmarBot(TelegramClient client) {
        this.client = client;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            LOGGER.info("Message received: {}", update.getMessage().getText());
            parseUrl(update.getMessage().getText());
            var rowsInline =
                    new InlineKeyboardRow(
                            List.of(
                                    InlineKeyboardButton.builder()
                                            .text("Visit Website")
                                            .url("https://www.example.com")
                                            .build()));

            var markupInline = new InlineKeyboardMarkup(List.of(rowsInline));
            var sendMessage =
                    SendMessage.builder()
                            .chatId(update.getMessage().getChatId().toString())
                            .text("Aquí está el link:")
                            .replyMarkup(markupInline)
                            .build();
            try {
                client.execute(sendMessage);
            } catch (TelegramApiException e) {
                LOGGER.error("Exception sending message", e);
            }
        }
    }
}
