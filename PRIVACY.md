# Privacy

Torrents is a BitTorrent client, so the one thing worth knowing is built into what it does:
it trades pieces of files directly with strangers' phones and computers.

## What leaves the phone

- **Your internet address, to the other people in each torrent.** That is how BitTorrent
  works. Everyone downloading or sharing the same torrent can see it while you are in it.
- **What you are downloading, to the trackers named in each torrent**, and to the public
  DHT network. The app joins the DHT by way of `dht.libtorrent.org`,
  `router.bittorrent.com`, `dht.transmissionbt.com` and `outer.silotis.us`.
- **Requests for the links and RSS feeds you give it.**

There is nothing else. No analytics, no telemetry, no advertising identifier, no update
check, and no account.

If you want your address hidden from the other people in a torrent, put the phone on a VPN
or set a proxy under *Settings → Network*, and turn on *Require proxy for all connections*.

## Crash reports

When the app crashes it offers to report it. Nothing is sent unless you press *Report*,
which opens your own mail app with the report addressed to `email@wanderthe.dev`, where you
can read it, change it or throw it away before it goes.

## Permissions

`app/src/main/AndroidManifest.xml` asks for:

| permission | why |
|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE` | to download and share, and to keep to Wi-Fi or unmetered connections when you ask it to |
| `WAKE_LOCK`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS` | to keep a download going with the screen off, and to say so in a notification |
| `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE` | to save files where you choose and to open `.torrent` files |
| `RECEIVE_BOOT_COMPLETED`, `QUICKBOOT_POWERON`, `SCHEDULE_EXACT_ALARM` | for the optional start-on-boot and the download scheduler |
| `VIBRATE` | for the optional buzz when a download finishes |
| `INSTALL_SHORTCUT` | for the "Add torrent" home-screen shortcut |

AndroidX also adds `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` to every app it builds. It lets
nothing in or out; it only keeps the app's own internal messages private to it.

## What is stored

Your torrents, their files, your feeds and your settings, on the phone and nowhere else. The
app does not allow Android's backup: nothing it keeps goes to a cloud backup or is copied to a
new phone by Android's transfer. **Save the torrent list** (Settings, Storage) writes each
torrent's magnet link, name, folder and settings to one file you choose through the phone's own
file picker, and only when you ask; **Restore the torrent list** reads such a file the same way,
checks all of it first, and adds the torrents that are not here yet.

## Checking for yourself

```
aapt2 dump badging honryu.apk | grep uses-permission
```

<!-- shared-about privacy section -->
## Who we are

Torrents is made by wander wildwood, an independent developer: one person, not a company. wander wildwood is the data controller for anything the app itself handles, and that is very little. Torrents keeps what you put in it on your phone, apart from what it sends to each torrent's peers and trackers, the public DHT network and the sites of the links and feeds you add as described above, and sends nothing to the developer, so the developer holds no data about you.

## How to contact us

Write to email@wanderthe.dev about your data, this policy or anything else. The same address is in the app, under About.

## Your rights

If you are in the EU, the EEA or the UK, the GDPR gives you the right to:

- see the personal data held about you (access)
- have it corrected (rectification) or deleted (erasure)
- limit how it is used (restriction)
- object to its use (objection)
- get a copy to take elsewhere (portability)
- withdraw your consent at any time, where something rests on it
- complain to a data protection supervisory authority, for example the one where you live

Similar laws elsewhere, such as the CCPA in California, give you the right to know, delete and correct, and to opt out of the sale or sharing of your data. Nothing from Torrents is sold or shared.

Since Torrents keeps what it stores only on your phone, you can exercise most of these rights yourself: remove a torrent or a feed in the app, save the torrent list from Settings, clear the app's data in Android's settings, or uninstall the app, which deletes all of it. For anything you cannot do yourself, write to the address above.

Torrents connects to each torrent's peers and trackers, the public DHT network and the sites of the links and feeds you add. They are separate from this app and from its developer, and what they receive is covered by their own privacy policies; ask them about what they hold.
<!-- /shared-about privacy section -->
