package com.truelife.chat.model;

public class SliderModel {

    private int featured_image;
    private String the_caption_Title;
    private String the_caption_Desc;

    public SliderModel(int hero, String title, String desc) {
        this.featured_image = hero;
        this.the_caption_Title = title;
        this.the_caption_Desc = desc;
    }

    public int getFeatured_image() {
        return featured_image;
    }

    public String getThe_caption_Title() {
        return the_caption_Title;
    }

    public void setFeatured_image(int featured_image) {
        this.featured_image = featured_image;
    }

    public void setThe_caption_Title(String the_caption_Title) {
        this.the_caption_Title = the_caption_Title;
    }

    public String getThe_caption_Desc() {
        return the_caption_Desc;
    }

    public void setThe_caption_Desc(String the_caption_Desc) {
        this.the_caption_Desc = the_caption_Desc;
    }
}