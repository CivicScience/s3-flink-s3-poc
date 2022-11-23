package com.civicscience.entity;

import lombok.*;


@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JotLog {

    private Integer sequence;
    private String id;
    private String ts;
    private boolean consent_accepted;
    private Integer meta_target_id;
    private Integer target_id;
    private String state;
    private String space;
    private Integer age;
    private Integer gender;
    private String locale;
    private String questions;
    private String context;
    private String user_alias;
    private boolean is_container_seen;
    private String usage;
    private Integer position;
    private String askable;
    private String user_session;
    private String natures;
    private String user_agent;
    private boolean is_jot;
    private String x_forwarded_for;
    private String session_template;
    private String session_template_group;
    private String ad_id;
    private String platform;

}
