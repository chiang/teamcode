19 November 2010

# LFTP Cheatsheet
I don't use FTP personally. But, on a professional level, LFTP is becoming a super-reliable option for me when automating FTP transfers with script. LFTP is a more robust FTP client than just plain FTP or cURL. Unlike those clients, it retries a few times when transmission fails, has mirroring features, and supports simultaneous multi-file transfers, recursion, and Regular-Expression matches. Speed, from what I've experienced, is identical.

## Upload a File

```
lftp -e 'put /local/path/yourfile.mp4; bye' -u user,password ftp.foo.com
```

Like all FTP clients, LFTP correctly uses Passive mode by default. Passive is newer than Active. It was designed to solve the problems with Active, and will go through the greatest array of firewalls without incident. [Never use Active.]

## Connection Timeout
LFTP will keep trying forever I believe, so it's wise to specify a timeout, especially if you are scripting this. In this example, timeout is 10 seconds:

```
lftp -e 'set net:timeout 10; put /local/path/yourfile.mp4; bye' -u user,password ftp.foo.com
```

## Active Mode
For what it's worth, Active mode -- older, stinkier, and won't go through your firewall:

```
lftp -e 'set ftp:passive-mode false; set net:timeout 10; put /local/path/yourfile.mp4; bye' -u user,password ftp.foo.com
```

## Download 

Uses Binary mode by default:

```
lftp -e 'set net:timeout 10; get yourfile.mp4; bye' -u user,password ftp.foo.com
```

If you need to put it in a specific local directory:

```
lftp -e 'set net:timeout 10; get yourfile.mp4 -o /local/path/yourfile.mp4; bye' -u user,password ftp.foo.com
```

## Mirroring

### Recursive Upload
When using the ``mirror`` command, LFTP is recursive by default.

Mirror everything from the ``/local/path`` to the root of the remote FTP site, including all subdirectories and their files. The -R switch means "reverse mirror" which means "put" [upload]. Remote path is simply ``/``, the root.

```
lftp -e 'mirror -R /local/path/ /' -u user,password ftp.foo.com
```

### Recursive Download
To mirror remote to local, just omit the ``-R`` param and swap remote path with local. Be careful with this. Don't overwrite your local changes.

```
lftp -e 'mirror / /local/path/' -u user,password ftp.foo.com
```

### Regular-Expression–Match Upload
Only include certain file types. Mirror everything in the ``/local/path`` recursively, but only transfer files that end with ``.htm,`` ``.html``, ``.css``, and ``.js``.

```
lftp -e 'mirror -R -i "\.(html?|css|js)$" /local/path/ /' -u user,password ftp.foo.com
```

### Non-Recursive
Deploy only root-level files, not the entire directory tree. The ``-r`` switch disables recursion. In this example, we are also only deploying HTML, CSS, and JavaScript:

```
lftp -e 'mirror -R -r -i "\.(html?|css|js)$" /local/path/ /' -u user,password ftp.foo.com
```

## Special Characters in Username or Password
Special characters in your username or password must be escaped with backslashes:

```
lftp -e 'put /local/path/yourfile.mp4; bye' -u user,password\!\! ftp.foo.com
```

IMO, FTP is not the right protocol for Internet file transfer of any kind. But, unfortunately, it is in place everywhere so you do find yourself stuck with it as the only option from time to time. [SFTP is the right protocol.]