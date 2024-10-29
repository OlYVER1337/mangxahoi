import React from 'react';
import { Container } from '@mui/material';
import UserList from './components/UserList'; // Đường dẫn đến UserList

const App = () => {
    return (
        <Container>
            <h1>Quản lý Người Dùng</h1>
            <UserList />
        </Container>
    );
};

export default App;
