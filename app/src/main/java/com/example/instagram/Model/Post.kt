package com.example.instagram.Model

class Post {
    private var postid: String = ""
    private var postimage: String = ""
    private var publisher: String = ""
    private var description: String = ""

    constructor()
    constructor(postid: String, postimage: String, publisher: String, description: String) {
        this.postid = postid
        this.postimage = postimage
        this.publisher = publisher
        this.description = description
    }

    fun getPostid(): String{
        return postid
    }

    fun setPostid(id: String){
        this.postid = id
    }

    fun getPostimage(): String{
        return postimage
    }

    fun setPostimage(image: String){
        this.postimage = image
    }

    fun getPublisher(): String{
        return publisher
    }

    fun setPublisher(publisher: String){
        this.publisher = publisher
    }

    fun getDescription(): String{
        return description
    }

    fun setDescription(description: String){
        this.description = description
    }

}