package sak.metricstool.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * SETTINGSテーブル
 * システム設定をキー・バリューで保持します。
 */
@Entity
@Table(name = "settings")
@Data
public class Settings {

    @Id
    @Column(name = "setting_key", length = 50)
    private String settingKey;

    @Column(name = "setting_value", length = 200)
    private String settingValue;
}
