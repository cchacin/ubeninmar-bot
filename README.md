# Ubeninmar Bot

A Telegram bot that automatically processes Amazon links in messages and converts them to affiliate links. The bot detects Amazon URLs (including shortened a.co links), extracts product ASINs, and responds with affiliate-tagged versions.

## Features

- **Amazon Link Detection**: Identifies Amazon URLs across all international domains
- **Short Link Resolution**: Follows a.co redirects to extract product information
- **Affiliate Link Generation**: Automatically adds affiliate tags to Amazon URLs
- **Multi-domain Support**: Works with amazon.com, amazon.co.uk, amazon.de, and other international domains
- **Robust Logging**: Comprehensive logging for monitoring and debugging
- **Clean Response Format**: Simple 💰 emoji prefix with affiliate URL

## Prerequisites

- Java 23
- Maven 3.9.9
- Telegram Bot Token (obtained from @BotFather)
- Amazon Affiliate Tag

## Quick Start

### 1. Environment Setup

Copy the example environment file and configure it:

```bash
cp .env.example .env
```

Edit `.env` with your configuration:

```bash
BOT_TOKEN=your_telegram_bot_token_here
AFFILIATE_TAG=your_amazon_affiliate_tag
```

### 2. Build and Run

Using Maven:

```bash
./mvnw clean compile exec:java -Dexec.mainClass="org.acme.Main"
```

Using Docker:

```bash
docker-compose up --build
```

### 3. Test the Bot

1. Start a chat with your bot on Telegram
2. Send a message containing an Amazon link
3. The bot will respond with the affiliate-tagged version

## Architecture

### Core Components

- **UbeninmarBot**: Main Telegram bot handler implementing message processing
- **LinkProcessor**: Orchestrates URL extraction and processing logic
- **AmazonLinkService**: Handles Amazon URL detection, ASIN extraction, and link processing
- **AffiliateService**: Manages affiliate tag insertion and URL construction
- **HttpRedirectFollower**: Follows redirects to resolve shortened URLs
- **ProcessedLink**: Data model representing processed link results

### Message Processing Flow

```
Telegram Message → URL Extraction → Amazon Detection →
Link Processing → Affiliate Tag Addition → Response Generation
```

### Supported URL Formats

- Standard Amazon URLs: `https://amazon.com/dp/B01234567`
- International domains: `https://amazon.co.uk/gp/product/B01234567`
- Shortened links: `https://a.co/d/abcdefg`
- Various path formats: `/dp/`, `/gp/product/`, `/product/`

## Configuration

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `BOT_TOKEN` | Telegram Bot API token from @BotFather | Yes |
| `AFFILIATE_TAG` | Your Amazon affiliate tag | Yes |

### Logging Configuration

The application uses SLF4J with Logback for logging. Log levels can be configured through standard Logback configuration files.

## Development

### Project Structure

```
src/main/java/org/acme/
├── Main.java                    # Application entry point
├── UbeninmarBot.java           # Telegram bot implementation
├── model/
│   ├── LinkType.java           # Enumeration of supported link types
│   └── ProcessedLink.java      # Link processing result data class
├── service/
│   ├── AffiliateService.java   # Affiliate tag management
│   ├── AmazonLinkService.java  # Amazon URL processing
│   └── LinkProcessor.java      # Main processing orchestrator
└── util/
    └── HttpRedirectFollower.java # HTTP redirect resolution
```

### Testing

Run unit tests:
```bash
./mvnw test
```

Run integration tests:
```bash
./mvnw integration-test
```

### Code Style

- Uses `.editorconfig` for consistent formatting
- Follows standard Java naming conventions
- Comprehensive logging with appropriate levels
- Defensive programming with null checks and validation

## Deployment

### Docker Deployment

1. Build the Docker image:
   ```bash
   docker build -t ubeninmar-bot .
   ```

2. Run with docker-compose:
   ```bash
   docker-compose up -d
   ```

### Manual Deployment

1. Package the application:
   ```bash
   ./mvnw clean package
   ```

2. Run the JAR file:
   ```bash
   java -jar target/ubeninmar-bot-*.jar
   ```

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions.

## License

This project is released under the Unlicense - see [UNLICENSE](UNLICENSE) file for details.

## Support

For issues and questions:
1. Check the logs for error messages
2. Verify environment variable configuration
3. Test with simple Amazon URLs first
4. Create an issue in the project repository