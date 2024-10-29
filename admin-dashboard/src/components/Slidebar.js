import React from 'react';
import { List, ListItem, ListItemText } from '@mui/material';

const Sidebar = ({ onSelect }) => {
    return (
        <List component="nav">
            <ListItem button onClick={() => onSelect("users")}>
                <ListItemText primary="Quản lý người dùng" />
            </ListItem>
            <ListItem button onClick={() => onSelect("posts")}>
                <ListItemText primary="Quản lý bài viết" />
            </ListItem>
        </List>
    );
};
