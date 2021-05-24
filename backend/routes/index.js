const express = require('express');
const router = express.Router();

router.get('/', function(req, res, next) {
  res.send('testing web socket');
});
//router.use('/images', require('./images'));

module.exports = router;
