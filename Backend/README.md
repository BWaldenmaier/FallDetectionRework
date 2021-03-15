# Falldetection Prototype

### On Server:

#### See Backend logs:

`journalctl -fn 100 -u falldetection.service`

#### Restart, start, stop Backend:

`sudo systemctl restart falldetection.service`

`sudo systemctl start falldetection.service`

`sudo systemctl stop falldetection.service`

#### Other info:

Edit Service file or process.env:

`sudo nano /etc/systemd/system/falldetection.service`
