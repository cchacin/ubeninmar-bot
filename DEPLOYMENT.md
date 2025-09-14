# Deployment Guide

## Quick Start

1. **Set up your Telegram bot**:
   ```bash
   # Get bot token from @BotFather in Telegram
   cp .env.example .env
   # Edit .env and add your BOT_TOKEN
   ```

2. **Run with Docker Compose**:
   ```bash
   docker-compose up --build -d
   ```

3. **Test the bot**:
    - Send any Amazon link to your bot
    - Send an `a.co` short link to test redirect handling
    - Bot will respond with affiliate link using `ubeferrer-20`

## Manual Testing

### Standard Amazon URLs:

- `https://amazon.com/dp/B07XYZ1234`
- `https://amazon.co.uk/gp/product/B07ABC5678`

### Short URLs (requires internet):

- `https://a.co/d/abc123` (will follow redirect)

## Expected Response Format:

```
💰 https://amazon.com/dp/B07XYZ1234?tag=ubeferrer-20
```

## Architecture Overview

```
Message → LinkProcessor → AmazonLinkService → AffiliateService
                       ↓
                  HttpRedirectFollower (for a.co links)
```

## Future Extensions

This modular architecture supports easy additions:

### 1. Other Affiliate Networks

Add new `*LinkService` classes following the same pattern:

```java
public class EbayLinkService {
    public ProcessedLink processEbayUrl(String url) { ...}
}
```

### 2. User-Specific Affiliate IDs

Modify `AffiliateService` to accept user context:

```java
public String addAffiliateTag(String url, String asin, String userId) { ...}
```

### 3. Link Analytics

Add tracking to `ProcessedLink`:

```java
public record ProcessedLink(
        // existing fields...
        String trackingId,
        Instant processedAt
) { ...
}
```

## Development

### Build and test:

```bash
mvn test
mvn package -Dshade
```

### Docker build:

```bash
docker build -t ubeninmar-bot .
```

### Local development:

```bash
export BOT_TOKEN=your_token_here
java -jar target/lib-1.0-SNAPSHOT.jar
```