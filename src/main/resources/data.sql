-- Events initial data
INSERT INTO events (event_id, event_name, start_date, end_date, created_at, updated_at) VALUES
('event1', 'Spring Boot Hackathon', '2024-01-01 09:00:00', '2024-01-07 18:00:00', NOW(), NOW()),
('event2', 'Java Conference 2024', '2024-02-15 10:00:00', '2024-02-17 16:00:00', NOW(), NOW());

-- Teams initial data
INSERT INTO teams (team_name, event_id, created_at, updated_at) VALUES
('Team Alpha', 'event1', NOW(), NOW()),
('Team Beta', 'event1', NOW(), NOW()),
('Team Gamma', 'event2', NOW(), NOW());

-- Participants initial data
INSERT INTO participants (participant_name, team_id, participant_gitlab_id, participant_gitlab_email, created_at, updated_at) VALUES
('Alice', 1, 'alice123', 'alice@example.com', NOW(), NOW()),
('Bob', 1, 'bob456', 'bob@example.com', NOW(), NOW()),
('Charlie', 2, 'charlie789', 'charlie@example.com', NOW(), NOW()),
('Dave', 3, 'dave321', 'dave@example.com', NOW(), NOW());

-- ParticipantMetrics initial data
INSERT INTO participant_metrics (participant_id, commits, deployment_frequency, change_failure_rate, change_lead_time, metric_date, created_at, updated_at) VALUES
(1, 15, 5, 0.1, 2.5, '2024-01-02', NOW(), NOW()),
(2, 20, 7, 0.2, 1.8, '2024-01-03', NOW(), NOW()),
(3, 10, 3, 0.3, 3.0, '2024-01-04', NOW(), NOW()),
(4, 12, 4, 0.15, 2.2, '2024-02-16', NOW(), NOW());

-- TeamMetrics initial data
INSERT INTO team_metrics (team_id, commits, std_dev_commits, deployment_frequency, change_failure_rate, change_lead_time, metric_date, created_at, updated_at) VALUES
(1, 35, 2.3, 12, 0.18, 2.1, '2024-01-05', NOW(), NOW()),
(2, 30, 2.1, 10, 0.22, 2.4, '2024-01-06', NOW(), NOW()),
(3, 28, 2.0, 8, 0.25, 3.2, '2024-02-17', NOW(), NOW());
