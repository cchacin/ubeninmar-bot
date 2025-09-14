# Deployment Guide

This guide covers all deployment scenarios for the Ubeninmar Telegram bot, from local development to production environments.

## Prerequisites

Before deploying, ensure you have:

1. **Telegram Bot Token**: Obtain from [@BotFather](https://t.me/botfather)
2. **Amazon Affiliate Tag**: Register with [Amazon Associates](https://affiliate-program.amazon.com)
3. **Java 23**: Required runtime environment
4. **Docker** (optional): For containerized deployment

## Environment Configuration

### 1. Environment Variables Setup

Copy and configure the environment file:

```bash
cp .env.example .env
```

Edit `.env` with your credentials:

```bash
# Telegram Bot Configuration
BOT_TOKEN=123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZ

# Amazon Affiliate Configuration
AFFILIATE_TAG=your-affiliate-tag-20
```

### 2. Obtaining Bot Token

1. Start a chat with [@BotFather](https://t.me/botfather)
2. Send `/newbot` command
3. Choose a name and username for your bot
4. Save the provided token securely

### 3. Amazon Affiliate Setup

1. Register at [Amazon Associates](https://affiliate-program.amazon.com)
2. Complete account verification
3. Note your affiliate tag (usually ends with `-20`)

## Deployment Options

### Option 1: Docker Compose (Recommended)

**Advantages:**
- Simple one-command deployment
- Automatic restart on failure
- Environment isolation
- Log management

**Steps:**

1. **Quick Start:**
   ```bash
   docker-compose up --build -d
   ```

2. **View Logs:**
   ```bash
   docker-compose logs -f
   ```

3. **Stop Service:**
   ```bash
   docker-compose down
   ```

### Option 2: Docker Manual

For more control over the container configuration:

1. **Build Image:**
   ```bash
   docker build -t ubeninmar-bot .
   ```

2. **Run Container:**
   ```bash
   docker run -d \
     --name ubeninmar-bot \
     --env-file .env \
     --restart unless-stopped \
     ubeninmar-bot
   ```

3. **View Logs:**
   ```bash
   docker logs -f ubeninmar-bot
   ```

### Option 3: Native Java

For environments where Docker is not available:

1. **Build Application:**
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Run Application:**
   ```bash
   export BOT_TOKEN=your_token_here
   export AFFILIATE_TAG=your_affiliate_tag
   java -jar target/ubeninmar-bot-1.0-SNAPSHOT.jar
   ```

3. **Background Execution:**
   ```bash
   nohup java -jar target/ubeninmar-bot-1.0-SNAPSHOT.jar > bot.log 2>&1 &
   ```

### Option 4: Maven Direct

For development and testing:

```bash
./mvnw clean compile exec:java -Dexec.mainClass="org.acme.Main"
```

## Production Deployment

### Cloud Platforms

#### Docker-Based Platforms (DigitalOcean, AWS ECS, Google Cloud Run)

1. **Create Dockerfile** (already included)
2. **Set Environment Variables** in platform console
3. **Deploy Container** using platform-specific instructions

#### Traditional VPS (Ubuntu/CentOS)

1. **Install Java 23:**
   ```bash
   # Ubuntu
   sudo apt update
   sudo apt install openjdk-23-jdk

   # CentOS
   sudo yum install java-23-openjdk-devel
   ```

2. **Create Service User:**
   ```bash
   sudo useradd -r -s /bin/false ubeninmar
   sudo mkdir /opt/ubeninmar-bot
   sudo chown ubeninmar:ubeninmar /opt/ubeninmar-bot
   ```

3. **Deploy Application:**
   ```bash
   # Copy JAR file
   sudo cp target/ubeninmar-bot-1.0-SNAPSHOT.jar /opt/ubeninmar-bot/

   # Create environment file
   sudo tee /opt/ubeninmar-bot/.env << EOF
   BOT_TOKEN=your_token_here
   AFFILIATE_TAG=your_affiliate_tag
   EOF
   ```

4. **Create Systemd Service:**
   ```bash
   sudo tee /etc/systemd/system/ubeninmar-bot.service << EOF
   [Unit]
   Description=Ubeninmar Telegram Bot
   After=network.target

   [Service]
   Type=simple
   User=ubeninmar
   WorkingDirectory=/opt/ubeninmar-bot
   EnvironmentFile=/opt/ubeninmar-bot/.env
   ExecStart=/usr/bin/java -jar ubeninmar-bot-1.0-SNAPSHOT.jar
   Restart=always
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   EOF

   sudo systemctl daemon-reload
   sudo systemctl enable ubeninmar-bot
   sudo systemctl start ubeninmar-bot
   ```

## Configuration Management

### Logging Configuration

The application uses Logback for logging. To customize log levels:

1. **Create `logback.xml`** in `src/main/resources/`:
   ```xml
   <configuration>
     <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
       <encoder>
         <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
       </encoder>
     </appender>

     <logger name="org.acme" level="INFO"/>
     <logger name="org.telegram" level="WARN"/>

     <root level="INFO">
       <appender-ref ref="STDOUT" />
     </root>
   </configuration>
   ```

2. **Log Levels:**
   - `ERROR`: Critical issues requiring immediate attention
   - `WARN`: Unexpected conditions that don't prevent operation
   - `INFO`: Normal operational messages
   - `DEBUG`: Detailed debugging information

### Memory Configuration

For production environments, tune JVM memory settings:

```bash
java -Xms256m -Xmx512m -jar ubeninmar-bot-1.0-SNAPSHOT.jar
```

### Network Configuration

The bot requires outbound HTTPS connections to:
- `api.telegram.org` (port 443) - Telegram Bot API
- `amazon.com` and international domains (port 443) - Link processing
- `a.co` (port 443) - Short link resolution

Ensure firewall rules allow these connections.

## Testing and Validation

### 1. Bot Functionality Test

Send test messages to your bot:

**Standard Amazon URLs:**
```
https://amazon.com/dp/B08N5WRWNW
https://amazon.co.uk/gp/product/B08N5WRWNW
https://www.amazon.de/dp/B08N5WRWNW
```

**Shortened URLs:**
```
https://a.co/d/abc123
```

**Expected Response Format:**
```
💰 https://amazon.com/dp/B08N5WRWNW?tag=your-affiliate-tag
```

### 2. Log Analysis

Monitor logs for errors or warnings:

```bash
# Docker Compose
docker-compose logs -f

# Systemd Service
sudo journalctl -u ubeninmar-bot -f

# Direct Log File
tail -f bot.log
```

### 3. Health Check Script

Create a simple health check:

```bash
#!/bin/bash
# health-check.sh

BOT_TOKEN="your_token_here"
CHAT_ID="your_test_chat_id"

curl -s "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage" \
  -d "chat_id=${CHAT_ID}" \
  -d "text=Health check: $(date)" \
  > /dev/null

if [ $? -eq 0 ]; then
    echo "Bot is responsive"
else
    echo "Bot is not responding"
fi
```

## Monitoring and Maintenance

### Application Monitoring

1. **Log Monitoring:** Watch for ERROR level messages
2. **Performance Metrics:** Monitor memory usage and response times
3. **Service Health:** Ensure the process remains running

### Telegram Bot Health

1. **Bot Status:** Use Telegram's `getMe` endpoint to verify bot status
2. **Webhook Status:** Monitor webhook delivery (if using webhooks)
3. **Rate Limits:** Watch for rate limiting messages from Telegram

### Amazon Affiliate Compliance

1. **Link Validation:** Ensure affiliate tags are being added correctly
2. **Commission Tracking:** Monitor Amazon Associates dashboard
3. **Terms Compliance:** Regularly review Amazon Associates terms

## Troubleshooting

### Common Issues

#### Bot Not Responding

1. **Check Environment Variables:**
   ```bash
   echo $BOT_TOKEN
   echo $AFFILIATE_TAG
   ```

2. **Verify Network Connectivity:**
   ```bash
   curl -s https://api.telegram.org/bot${BOT_TOKEN}/getMe
   ```

3. **Check Application Logs:**
   ```bash
   grep -i error bot.log
   ```

#### Links Not Processing

1. **Test URL Patterns:**
   - Ensure URLs match supported Amazon formats
   - Verify international domain support

2. **Check Redirect Resolution:**
   - Test a.co links manually
   - Monitor redirect following logs

#### Missing Affiliate Tags

1. **Environment Variable:** Verify `AFFILIATE_TAG` is set correctly
2. **Amazon Associates Account:** Ensure affiliate account is active
3. **URL Construction:** Check generated URLs contain proper tags

### Performance Issues

#### High Memory Usage

1. **Monitor JVM Memory:**
   ```bash
   jps -v | grep ubeninmar
   ```

2. **Adjust Heap Size:**
   ```bash
   java -Xmx256m -jar ubeninmar-bot-1.0-SNAPSHOT.jar
   ```

#### Slow Response Times

1. **Network Latency:** Check connectivity to Telegram and Amazon
2. **Redirect Resolution:** Monitor time taken for a.co link resolution
3. **Resource Constraints:** Ensure adequate CPU and memory

## Security Considerations

### Environment Security

1. **Token Protection:**
   - Never commit `.env` files to version control
   - Use platform-specific secret management in production
   - Rotate tokens regularly

2. **File Permissions:**
   ```bash
   chmod 600 .env
   chmod 600 /opt/ubeninmar-bot/.env
   ```

3. **Service User:** Run the bot with minimal privileges

### Network Security

1. **Firewall Configuration:** Allow only necessary outbound connections
2. **TLS Verification:** Ensure all HTTPS connections verify certificates
3. **Rate Limiting:** Implement appropriate rate limiting for API calls

### Data Privacy

1. **Message Logging:** Avoid logging user message content in production
2. **Data Retention:** Don't store user messages or personal information
3. **GDPR Compliance:** Consider data protection requirements

## Scaling and Extensions

### Horizontal Scaling

For high-volume deployments:

1. **Load Balancing:** Use multiple bot instances with different tokens
2. **Database Integration:** Add persistent storage for analytics
3. **Queue Processing:** Implement message queue for processing

### Feature Extensions

The modular architecture supports easy additions:

#### 1. Additional Affiliate Networks

```java
public class EbayLinkService {
    public ProcessedLink processEbayUrl(String url) {
        // Implementation for eBay affiliate links
    }
}
```

#### 2. User-Specific Configuration

```java
public class UserAffiliateService {
    public String getAffiliateTag(String userId) {
        // Return user-specific affiliate tag
    }
}
```

#### 3. Analytics and Tracking

```java
public class LinkAnalytics {
    public void recordLinkProcessing(ProcessedLink link, String userId) {
        // Store analytics data
    }
}
```

## Support and Maintenance

### Regular Maintenance Tasks

1. **Update Dependencies:** Regularly update Maven dependencies
2. **Security Patches:** Apply security updates promptly
3. **Log Rotation:** Implement log rotation to manage disk space
4. **Backup Configuration:** Backup environment files and configurations

### Getting Help

1. **Check Logs:** Always start with application logs
2. **Test Environment:** Reproduce issues in a test environment
3. **Telegram Bot API:** Reference official Telegram Bot API documentation
4. **Amazon Associates:** Contact Amazon Associates support for affiliate issues

### Version Updates

1. **Backup Current Version:**
   ```bash
   cp ubeninmar-bot-1.0-SNAPSHOT.jar ubeninmar-bot-backup.jar
   ```

2. **Deploy New Version:**
   ```bash
   ./mvnw clean package -DskipTests
   sudo systemctl stop ubeninmar-bot
   sudo cp target/ubeninmar-bot-1.0-SNAPSHOT.jar /opt/ubeninmar-bot/
   sudo systemctl start ubeninmar-bot
   ```

3. **Verify Deployment:**
   ```bash
   sudo systemctl status ubeninmar-bot
   sudo journalctl -u ubeninmar-bot -f
   ```