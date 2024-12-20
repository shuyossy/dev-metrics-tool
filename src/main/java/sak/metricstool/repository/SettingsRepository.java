package sak.metricstool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sak.metricstool.entity.Settings;

public interface SettingsRepository extends JpaRepository<Settings, String> {
}
