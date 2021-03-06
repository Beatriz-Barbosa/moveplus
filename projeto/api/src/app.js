const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const routes = require('./routes');

const app = express();

app.use(cors());
app.use(bodyParser.urlencoded({ extended: true }));
routes.get('/teste', function(request, response) {
    response.json({
        msg: 'ok'
    })
});

app.use(routes);

module.exports = app;
