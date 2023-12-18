package com.lec.spring.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QryCommentList extends QryResult {
    @ToString.Exclude
    @JsonProperty("data")  // JSON 변환 시 "data"라는 이름의 property로 변환됨
    List<Comment> list;


}
