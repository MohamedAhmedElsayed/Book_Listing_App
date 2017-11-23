package com.example.mohamed_ahmed.books;

public class ListItem {
    String Title, PreviewLink, Description, Language, Authors;

    public ListItem(String title, String previewLink, String description, String language, String authors) {
        Title = title;
        PreviewLink = previewLink;
        Description = description;
        Language = language;
        Authors = authors;
    }

    public String getTitle() {
        return Title;
    }

    public String getPreviewLink() {
        return PreviewLink;
    }

    public String getDescription() {
        return Description;
    }

    public String getLanguage() {
        return Language;
    }

    public String getAuthors() {
        return Authors;
    }
}
