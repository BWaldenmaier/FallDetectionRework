const express = require('express');
const app = express();
const mysql = require('mysql');
const PushNotifications = require('@pusher/push-notifications-server');

//Pusher Service fpr Notification to the Application
let pushNotifications = new PushNotifications({
 instanceId: 'b147d8cf-58f4-4190-97bb-65410f817f68',
  secretKey: '58AA7997FCA2F07CED275E3128F8D4C35B366869A2783A2B8CFBAD861B3A7F10',
});

const port = 3000;

//create database connection 
var con = mysql.createConnection({
    host: process.env.EHEALTH_DB_HOST,
    user: process.env.EHEALTH_DB_USER,
    password: process.env.EHEALTH_DB_PW,
    database: process.env.EHEALTH_DB_NAME,
    insecureAuth: process.env.EHEALTH_DB_IA
});

app.use(express.json());


//connect to database 
con.connect(function (err) {
    if (err) throw err;
    console.log("Connected!");
});


app.get('/', (req, res) => res.send('Hello World!'));


//get a user by username 
app.get("/user", (req, res) => {
    console.log(req);
    con.query('SELECT * FROM user where username="' + req.body.username + '";', (err, result, fields) => {
        if (err) throw err;
        console.log(result[0]);
        res.json(result[0]);
    });
});


//login to the database 
app.post("/user/login", async (req, res) => {
    const username = req.body.username;
    const password = req.body.password;
    console.log('-------------------LOGIN-------------------');
    console.log('User ' + username + ' tries to log in...');
    await con.query('SELECT * FROM user where username="' + username + '" and password="' + password + '";', (error, results, fields) => {
        if (error) {
            console.log(error);
            console.log('----------------LOGIN FAILED---------------');
            res.json({ success: false });
            return;
        }
        if(results.length == 1) {
            console.log('------------------LOGIN OK-----------------');
            res.json({ success: true });
        }
        if((results.length == 0) || (results.length > 1)) {
            console.log('----------------LOGIN FAILED---------------');
            res.json({ success: false });
        }
    });
});

//register a new user 
//test
//test2
app.post("/user/registration", async (req, res) => {
    const fullname = req.body.fullname;
    const email = req.body.email;
    const username = req.body.username;
    const password = req.body.password;
    const arduinoID = req.body.arduinoID;
    console.log('-------------------Registration-------------------');
    console.log('User ' + username + ' tries to register');
    var sql = "INSERT INTO `user` (`fullname`, `email`, `username`, `password` , `arduinoID`) VALUES ('" + fullname + "','" + email + "','" + username + "','" + password + "','" + arduinoID + "')";
    await con.query(sql, function (err, result) {
        if (err) {
            if (err.code === 'ER_DUP_ENTRY') {
                console.log(err);
                console.log('----------------Registration FAILED---------------');
                res.json({ success: "doubleEntry" });
            }
            else {
                console.log(err);
                console.log('----------------Registration FAILED---------------');
                res.json({ success: false });
            }
           
        }
        else {
            console.log('------------------Registration Success-----------------');
            res.json({ success: true });
        }        
    });
});


//post all detected falls to a specific username 
app.post('/updateFalls', async (req, res) => {
    var username = req.body
    console.log('-------------------Update-------------------');
    console.log('User ' + username + ' tries to update falls');
    await con.query('SELECT * FROM fall WHERE arduinoID= ( SELECT arduinoID FROM user WHERE username="' + username + '");', (error, results, fields) => {
        if (error) console.log(error);
        res.json(results);
    });
});

//This Endpoint will be triggered from the Arduino and send the alert to the application
app.post('/alert', async (req, res) => {
    var arduinoID = req.body.arduinoID;
    var now = new Date();
    var year = now.getFullYear();
    var month = ((now.getMonth() + 1) < 10) ? '0' + (now.getMonth() + 1) : (now.getMonth() + 1);
    var day = (now.getDate() < 10) ? '0' + now.getDate() : now.getDate();
    var hour = (now.getHours() < 10) ? '0' + now.getHours() : now.getHours();
    var minutes = (now.getMinutes() < 10) ? '0' + now.getMinutes() : now.getMinutes();
    var seconds = (now.getSeconds() < 10) ? '0' + now.getSeconds() : now.getSeconds()
    var date = year + '-' + month + '-' + day + ' ' + hour + ':' + minutes + ':' + seconds;

    var status = 'OK';
    await pushNotifications
        .publishToInterests(['hello'], {
            fcm: {
                notification: {
                    title: '⚠️URGENT⚠️',
                    body: 'A fall was just detected on one of your devices!',
                },
                data: {
                    arduinoID: arduinoID,
                    date: date
                },
            },
        })
        .then(publishResponse => {
            console.log('Just published:', publishResponse.publishId);
        })
        .catch(error => {
            console.log('Pusher Error: ', error);
            status = 'NOTIF_ERR';
        });
    await con.query('INSERT INTO fall (id, date, arduinoID) VALUES (0, "' + date + '",' + arduinoID + ' );', (error, results, fields) => {
        if (error) {
            console.log('DB Error: ' + error);
            status = (status == 'NOTIF_ERR') ? 'NOTIF_DB_ERR' : 'DB_ERR';
        }
        res.json({
            status: status
        });
    });
    
});

app.listen(port, () => console.log(`FallDetection server running and listening on port ${port}!`));
