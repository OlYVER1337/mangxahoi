import React, { useState, useEffect } from "react";
import { MdOutlineClose } from "react-icons/md";
import { FiMoreHorizontal } from "react-icons/fi";
import { FaGlobeAmericas } from "react-icons/fa";
import { AiOutlineLike } from "react-icons/ai";
import { IoArrowUpCircleSharp } from "react-icons/io5";
import { TfiComment } from "react-icons/tfi";
import { useSession } from "next-auth/react";
import { deleteDoc, doc, updateDoc, arrayUnion, arrayRemove, addDoc, collection, serverTimestamp, onSnapshot } from "firebase/firestore";
import { db } from "../firebase";

const Post = ({ data, id }) => {
    const { data: session } = useSession();
    const [likeBy, setLikeBy] = useState(data.likedBy || []);
    const [hasLikedby, setHasLikedby] = useState(false);
    const [comments, setComments] = useState([]);
    const [commentInput, setCommentInput] = useState("");
    const [showCommentInput, setShowCommentInput] = useState(false);


    useEffect(() => {
        setHasLikedby(likeBy.includes(session.user.id));
    }, [likeBy]);

    const handleLike = async () => {
        if (hasLikedby) {
            await updateDoc(doc(db, "posts", id), {
                likedBy: arrayRemove(session.user.id),
            });
            setLikeBy(likeBy.filter((id) => id !== session.user.id));
        } else {
            await updateDoc(doc(db, "posts", id), {
                likedBy: arrayUnion(session.user.id),
            });
            setLikeBy([...likeBy, session.user.id]);
        }
    };
  useEffect(() => {
    setHasLikedby(likeBy.includes(session.user.id));
  }, [likeBy]);

  const handleLike = async () => {
    if (hasLikedby) {
      await updateDoc(doc(db, "posts", id), {
        likedBy: arrayRemove(session.user.id),
      });
      setLikeBy(likeBy.filter((id) => id !== session.user.id));
    } else {
      await updateDoc(doc(db, "posts", id), {
        likedBy: arrayUnion(session.user.id),
      });
      setLikeBy([...likeBy, session.user.id]);
    }
  };


    const isAdmin = (post_data_id, session_id) => {
        if (post_data_id === session_id) return true;
        return false;
    };

    const handleComment = async () => {
        if (commentInput.trim()) {
            const newComment = {
                content: commentInput,
                imageUrl: null,
                timestamp: serverTimestamp(),
                userId: session.user.id,
                userImage: session.user.image,
                userName: session.user.name,
            };
            const docRef = await addDoc(collection(db, "posts", id, "comments"), newComment);
            setComments([...comments, { id: docRef.id, ...newComment }]);
            setCommentInput("");
        }
    };

  const handleComment = async () => {
    if (commentInput.trim()) {
      const newComment = {
        content: commentInput,
        imageUrl: null,
        timestamp: serverTimestamp(),
        userId: session.user.id,
        userImage: session.user.image,
        userName: session.user.name,
      };
      const docRef = await addDoc(collection(db, "posts", id, "comments"), newComment);
      setComments([...comments, { id: docRef.id, ...newComment }]);
      setCommentInput("");
    }
  };


    const toggleComments = () => {
        setShowCommentInput(!showCommentInput);
        if (!showCommentInput) {
            onSnapshot(collection(db, "posts", id, "comments"), (snapshot) => {
                setComments(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
            });
        }
    };

    useEffect(() => {
        const unsubscribe = onSnapshot(collection(db, "posts", id, "comments"), (snapshot) => {
            setComments(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
        });
        return () => unsubscribe(); // Clean up the subscription on unmount
    }, [id]);

    const formatTimestamp = (timestamp) => {
        const now = new Date();
        const postDate = new Date(timestamp);
        const diffInMs = now - postDate;
        const diffInMinutes = Math.floor(diffInMs / 60000);
        const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
        const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

        if (diffInDays > 0) {
            return `${diffInDays} day${diffInDays !== 1 ? 's' : ''} ago`;
        } else if (diffInHours > 0) {
            return `${diffInHours} hour${diffInHours !== 1 ? 's' : ''} ago`;
        } else {
            return `${diffInMinutes} minute${diffInMinutes !== 1 ? 's' : ''} ago`;
        }
    };

    return (
        <div className="py-4 bg-white rounded-[17px] shadow-md mt-5">
            <div className="px-4 flex justify-between items-center">
                <div className="flex gap-2">
                    <img
                        className="w-[44px] h-[44px] object-cover rounded-full"
                        src={data.userImage}
                        alt="dp"
                    />
                    <div>
                        <h1 className="text-[16px] font-semibold">{data.userName}</h1>
                        <div className="text-gray-500 flex items-center gap-2">
                            <p>{data.postTimestamp ? formatTimestamp(data.postTimestamp.toDate()) : 'Unknown time'}</p>
                            <p>·</p>
                            <FaGlobeAmericas />
                        </div>
                    </div>
                </div>

                <div className="text-gray-500 text-[26px] flex gap-4">
                    <FiMoreHorizontal className="cursor-pointer" />
                    {isAdmin(data.userId, session.user.id) && (
                        <MdOutlineClose
                            className="cursor-pointer"
                            onClick={() => {
                                if (window.confirm("Delete this post?")) {
                                    deleteDoc(doc(db, "posts", id));
                                }
                            }}
                        />
                    )}
                </div>
            </div>


            <p className="px-4 mt-[15px] text-gray-800 font-normal">{data.content}</p>

        <div className="text-gray-500 text-[26px] flex gap-4">
          <FiMoreHorizontal className="cursor-pointer" />
          {isAdmin(data.userId, session.user.id) && (
            <MdOutlineClose
              className="cursor-pointer"
              onClick={() => {
                if (window.confirm("Delete this post?")) {
                  deleteDoc(doc(db, "posts", id));
                }
              }}
            />
          )}
        </div>
      </div>


            <div className="mt-[15px]">
                {data.postImage && <img src={data.postImage} alt="post pic" />}
            </div>

            <div className="mx-4 h-[1px] bg-gray-300 mt-[15px]"></div>

            <div className="flex mt-[7px] text-gray-500">
                <div className="flex gap-2 justify-center items-center w-[50%] py-2 rounded-[10px] hover:bg-gray-200 cursor-pointer" onClick={handleLike}>
                    <AiOutlineLike className={`text-[26px] ${hasLikedby ? "text-blue-500" : ""}`} />
                    <p className="font-medium">{likeBy.length} Like{likeBy.length !== 1 ? "s" : ""}</p>
                </div>
                <div className="flex gap-2 justify-center items-center w-[50%] py-2 rounded-[10px] hover:bg-gray-200 cursor-pointer" onClick={toggleComments}>
                    <TfiComment className="text-[20px] translate-y-[4px]" />
                    <p className="font-medium">{comments.length} Comment{comments.length !== 1 ? "s" : ""}</p>
                </div>
            </div>

            {showCommentInput && (
                <div>
                    {comments.map((comment) => (
                        <div key={comment.userId} className="flex gap-2 mt-2">
                            <img src={comment.userImage} alt="user" className="w-8 h-8 rounded-full" />
                            <div>
                                <p className="font-semibold">{comment.userName}</p>
                                <p>{comment.content}</p>
                            </div>
                        </div>
                    ))}
                    <div className="relative">
                        <input
                            type="text"
                            value={commentInput}
                            onChange={(e) => setCommentInput(e.target.value)}
                            placeholder="Add a comment..."
                            className="w-full p-2 border rounded pr-12"
                        />
                        <button onClick={handleComment} className="absolute right-2 top-1/2 transform -translate-y-1/2 text-[35px]">
                            <IoArrowUpCircleSharp />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Post;