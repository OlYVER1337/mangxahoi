import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import './index.css'; // Đảm bảo rằng đường dẫn này là chính xác

ReactDOM.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>,
    document.getElementById('root')
);
