# Discord Multi Webhook Notifier

A RuneLite plugin that sends notifications to multiple Discord webhooks based on in-game events.

## Features

- Send notifications for various in-game events:
  - Collection log entries
  - Pet drops
  - Level ups
  - Valuable loot (with GP value threshold)
  - Deaths
  - Quest completions
  - Clue scroll completions
- Configure a different Discord webhook for each type of event
- Add screenshots to your notifications
- Intelligent grouping of related notifications to avoid spam
- Highly customizable notification messages

## Setup

1. Install the plugin from the Plugin Hub
2. Open the plugin configuration
3. Enter at least a Global Webhook URL (required for basic functionality)
4. Enable the notification types you want to receive
5. Optionally configure custom webhooks for specific event types

## Discord Webhook Configuration

To create a Discord webhook:
1. Open Discord
2. Go to Server Settings > Integrations > Webhooks
3. Click "New Webhook"
4. Choose a name and channel
5. Copy the Webhook URL
6. Paste into the plugin config

## Testing

You can test the plugin using the following commands in-game:
- `::dwtest col` - Test collection log notification
- `::dwtest pet` - Test pet notification
- `::dwtest lvl` - Test level up notification
- `::dwtest loot` - Test valuable drop notification
- `::dwtest death` - Test death notification
- `::dwtest quest` - Test quest completion notification
- `::dwtest clue` - Test clue scroll notification
- `::dwtest coldrp` - Test combined collection log + drop notification
- `::dwtest all` - Test all notification types

Add `img` to any command to include a screenshot: `::dwtest col img`

## Customizing Notifications

The plugin supports customizing the message format for each notification type. You can use the following variables:

### Global Variables
- `%USERNAME%` - Your character's name

### Collection Log
- `%ITEM%` - The item added to the collection log

### Pet
- `%PET%` - The pet received

### Level Up
- `%SKILL%` - The skill leveled up
- `%LEVEL%` - The new level

### Loot
- `%ITEM%` - The item received
- `%VALUE%` - The value in GP (formatted with commas)

### Death
- `%LOCATION%` - Location of death
- `%CAUSE%` - Cause of death (if available)

### Quest
- `%QUEST%` - The completed quest

### Clue Scroll
- `%CLUETYPE%` - The type of clue scroll (Easy, Medium, Hard, Elite, Master)
- `%COUNT%` - Number of clues completed of this type
- `%REWARD%` - The reward received (if available)

## License

This plugin is released under the BSD 2-Clause License.

## Author

Created by [moseph](https://github.com/malkailany)