# Easy Server Manager
A simple Java application to make managing self-hosted Minecraft servers easier

### Features
 - Auto-reboot
 - Any version, any loader including vanilla*
 - Runs externally to Minecraft
 - Simple web interface
 - Add directly from Modrinth
 - Lightweight

\* must have `/stop` functional and `[Server thread/INFO]` log format
### Requirements
 - TMUX installed
 - Knowledge on how to normally run a server
 - Java installed

### Setup
1. Download latest from releases page
2. Place .jar and .sh files in your server's directory
3. Configure the `start_servermanager.sh` file (instructions in file)
4. Run `start_servermanager.sh`
5. Configure
6. Re-run `start_servermanager.sh`
7. Go to http://localhost:8082

### Configure the following:
**managerPort:** The port the webserver will be hosted on (default 8082)

**loginKey:** Set the login password (will be hashed/hidden after load)

**webhookURL:** Optional. Discord webhook for notifications for manager (eg, Server has rebooted)

**serverIp:** Your server's IP

**startCommand:** The start command (eg, `java -jar server.jar...`)