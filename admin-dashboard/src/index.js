const admin = require("firebase-admin");
const serviceAccount = require("./path/to/serviceAccountKey.json"); // Đường dẫn đến tệp JSON

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://<your-database-name>.firebaseio.com" // Thay <your-database-name> bằng tên cơ sở dữ liệu của bạn
});
