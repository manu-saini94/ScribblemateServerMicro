package com.scribblemate.entities.elastic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scribblemate.common.utility.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDocument {
    @Id
    @Field(type = FieldType.Keyword,name = "id")
    private String id;

    @Field(type = FieldType.Text, name = "fullName")
    private String fullName;

    @Field(type = FieldType.Keyword, name = "email")
    private String email;

    @Field(type = FieldType.Text, name = "password")
    @JsonIgnore
    private String password;

    @Field(type = FieldType.Keyword, name = "status")
    private Utils.Status status;

    @Field(type = FieldType.Date, name = "createdAt")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, name = "updatedAt")
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Text, name = "profilePicture")
    private String profilePicture;
}
