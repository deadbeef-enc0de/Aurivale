# Aurivale
Is a Folia first plugin that overhauls many aspects of the Minecraft experience with lite RPG elements.

More information about the changes it makes are in the wiki of this project.

It is unlikely that this plugin will play nice with other plugins that change the core systems of Minecraft that this plugin changes.

## Server Owners
Installing the plugin is as easy putting the jar into the plugins folder along with the dependencies.  

### Resource Pack
It is **HIGHLY** encouraged for players to use the resource pack that is part of the release.  The biggest issue is the delineation between axes (tool) and battleaxes (weapon), without it they will look the same.

### Configuration
The plugin has a default configuration that will be saved to disk on first run. Feel free to make changes as you see fit, the structure is fairly easy to understand. Check the wiki if you need help with configuring the plugin.

It is in your best interest to setup `server.properties` file to automatically tell the client to use the resource pack for this server.

Lastly the server configuration should be updated to not allow the end dimension as it is not within the scope of the plugin.

### Dependencies
This plugin depends on the following plugins
- **[Hard]** [EasyCommands](https://github.com/deadbeef-enc0de/EasyCommands/releases/latest)
- **[Hard]** [YamlSaver](https://github.com/deadbeef-enc0de/YamlSaver/releases/latest)
- **[Soft]** [Aurivale-ProtocolLib](https://github.com/deadbeef-enc0de/Aurivale-ProtocolLib/releases/latest)
  - **[Hard]** [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

## Issues
Users of the plugin are highly encouraged to make issues for **Bug Reports** and **Feature Requests**

Issues will be updated with comments and tags to let filers know of updates to the issue as well as the expected version that would contain the bug fix or feature requested.

### Bug Report
When you find a bug/issue with the plugin please include the following details:
- Detailed explanation of the bug including what you think the intended behavior is
- Steps to reproduce the bug
- Relevant changes to the configuration that might impact the bug

Remember to add the **bug** label to the issue while/after filing the issue.

### Feature Request
If you have an idea of a feature to be added to the plugin please file an issue with as much detail how what the feature is and how it should work. The more details in the feature request the better I can create it in this plugin.

**Note:** This is a server side plugin so I can accomodate features that touch the server side or can be put into a resource pack. Client side mods are not a feature I will entertain.

Remember to add the **feature request** label to the issue while/after filing the request.

## Pull Requests
Due to bandwidth contraints on my time it will be unlikely that I will have time to look at pull requests against this project.

# Special Thanks
To my wife who has put up with me while developing this over time.  
To my friends who have tested so many versions of this plugin with different features.  
Grinding Gear Games who I drew inspiration from to make this plugin and the enchanting system should be familiar to those that play Path of Exile.  
