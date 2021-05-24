const express = require('express');
const router = express.Router();
const imageController = require('../../controller/imageController');

router.post('/receive', imageController.receive);
router.post('/send', imageController.send);

module.exports = router;