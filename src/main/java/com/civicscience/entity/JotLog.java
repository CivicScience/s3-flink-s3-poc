package com.civicscience.entity;

import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JotLog {
    private Integer sequence;
    private String id;
    private String ts;
    private Boolean consent_accepted;
    private String meta_target_id;
    private String target_id;
    private String state;
    private String space;
    private Integer age;
    private Integer gender;
    private String locale;
    private List<String> questions;
    private String context;
    private String user_alias;
    private Boolean isContainerSeen;
    private String usage;
    private Integer position;
    private String askable;
    private String user_session;
    private List<String> natures;
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
    private String hashCode;

}
