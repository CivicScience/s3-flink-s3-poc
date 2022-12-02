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
    private String target_id;
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
    private String x_forwarded_for;
    private String session_template;
    private String session_template_group;
    private String ad_id;
    private String platform;
    private String ip_v4;
    private String ua_browser_family;
    private String ua_browser_version;
    private String ua_os_family;
    private String ua_os_version;
    private String ua_device_family;
    private String ua_device_class;
    private boolean ua_is_mobile;
    private boolean ua_is_bot;
    private String cs_id;
    private String language_region;
}
