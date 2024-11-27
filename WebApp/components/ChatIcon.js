import React from "react";
import { BsMessenger } from "react-icons/bs";

const ChatIcon = ({ onClick }) => {
    return (
        <div className="icon_wrapper text-[20px] cursor-pointer" onClick={onClick}>
            <BsMessenger />
        </div>
    );
};

export default ChatIcon;
