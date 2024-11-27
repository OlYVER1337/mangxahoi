import React from "react";

const ChatWindow = () => {
    return (
        <div className="fixed bottom-0 right-4 bg-white w-[350px] h-[500px] shadow-lg rounded-t-lg flex flex-col">
            <div className="bg-primary p-4 text-white rounded-t-lg">
                <h3 className="text-lg font-bold">Messenger</h3>
            </div>
            <div className="flex-grow overflow-y-auto p-4">
                <p>Start chatting...</p>
            </div>
            <div className="p-4 border-t">
                <input
                    className="w-full p-2 border rounded-md outline-none"
                    type="text"
                    placeholder="Type a message..."
                />
            </div>
        </div>
    );
};

export default ChatWindow;
