// src/main/resources/static/app.js
$(document).ready(function(){
    let currentArea = "#dashboard-area";
    showArea(currentArea);

    // DataTables初期化用関数
    function initDataTable(selector){
      if($.fn.DataTable.isDataTable(selector)){
        $(selector).DataTable().clear().destroy();
      }
      $(selector).DataTable({
        paging: true,
        searching: true,
        ordering: true,
        language: {
          url: "//cdn.datatables.net/plug-ins/1.13.4/i18n/Japanese.json"
        }
      });
    }

    // トースト表示用関数
    function showToast(message, type='success'){
      let bgClass = (type==='success')?'bg-success':'bg-danger';
      let $toast = $(`
        <div class="toast align-items-center text-white ${bgClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
          <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="閉じる"></button>
          </div>
        </div>
      `);
      $("#toast-container").append($toast);
      let toast = new bootstrap.Toast($toast[0], { delay: 3000 });
      toast.show();
      $toast.on('hidden.bs.toast', function () {
        $(this).remove();
      });
    }

    // ローディング表示制御
    function showLoading(){
      $("#loading-overlay").show();
    }
    function hideLoading(){
      $("#loading-overlay").hide();
    }

    // ナビゲーションクリックイベント
    $("#nav-dashboard").click(function(){showArea("#dashboard-area"); loadEventsForDashboard();});
    $("#nav-event").click(function(){showArea("#event-area"); loadEvents();});
    $("#nav-team").click(function(){showArea("#team-area"); loadTeams();});
    $("#nav-participant").click(function(){showArea("#participant-area"); loadParticipants();});
    $("#nav-team-metrics").click(function(){showArea("#team-metrics-area"); loadEventsForTeamMetrics();});
    $("#nav-participant-metrics").click(function(){showArea("#participant-metrics-area"); loadTeamsForParticipantMetrics();});
    $("#nav-settings").click(function(){showArea("#settings-area"); loadBranchRules();});

    function showArea(selector){
        $("#dashboard-area,#event-area,#team-area,#participant-area,#team-metrics-area,#participant-metrics-area,#settings-area").addClass("hidden");
        $(selector).removeClass("hidden");
        currentArea = selector;
    }

    // ダッシュボード関連
    function loadEventsForDashboard(){
      showLoading();
      $.get("/api/events", function(events){
          let select = $("#select-event-dashboard").empty();
          events.forEach(e=>{
            select.append($("<option>").val(e.eventId).text(e.eventName));
          });
          if(events.length>0){
            let selectedEventId = select.val();
            loadDashboardMetrics(selectedEventId);
          } else {
            hideLoading();
            showToast("イベントが登録されていません","error");
          }
      }).fail(function(){
        showToast("イベント取得に失敗しました","error");
        hideLoading();
      });
    }

    function loadDashboardMetrics(eventId){
      // ダッシュボード用として、イベントに紐づくチームメトリクスを取得
      $.ajax({
        url: "/api/team-metrics/event/" + encodeURIComponent(eventId),
        type: "GET",
        success: function(metrics){
          renderDashboardCharts(metrics);
          hideLoading();
        },
        error: function(){
          showToast("メトリクス取得に失敗しました","error");
          hideLoading();
        }
      });
    }

    function renderDashboardCharts(metrics){
      // データを日付順にソート
      metrics.sort((a, b) => new Date(a.metricDate) - new Date(b.metricDate));

      // メトリクスを日付ごとにグループ化
      let metricsByDate = {};
      metrics.forEach(m => {
        let date = m.metricDate;
        if (!metricsByDate[date]) {
          metricsByDate[date] = [];
        }
        metricsByDate[date].push(m);
      });

      // ソートされた日付リスト
      let sortedDates = Object.keys(metricsByDate).sort((a, b) => new Date(a) - new Date(b));

      // チームごとのメトリクスデータを準備
      let teams = [...new Set(metrics.map(m => m.team.teamName))];
      let datasetsCommits = [];
      let datasetsDeploymentFreq = [];
      let datasetsChangeFailureRate = [];
      let datasetsChangeLeadTime = [];

      teams.forEach((team, index) => {
        let color = getColor(index);
        let commitsData = [];
        let deploymentFreqData = [];
        let changeFailureRateData = [];
        let changeLeadTimeData = [];

        sortedDates.forEach(date => {
          let teamMetrics = metricsByDate[date].find(m => m.team.teamName === team);
          if (teamMetrics) {
            commitsData.push({x: date, y: teamMetrics.commits});
            deploymentFreqData.push({x: date, y: teamMetrics.deploymentFrequency});
            changeFailureRateData.push({x: date, y: teamMetrics.changeFailureRate});
            changeLeadTimeData.push({x: date, y: teamMetrics.changeLeadTime});
          } else {
            commitsData.push({x: date, y: null});
            deploymentFreqData.push({x: date, y: null});
            changeFailureRateData.push({x: date, y: null});
            changeLeadTimeData.push({x: date, y: null});
          }
        });

        datasetsCommits.push({
          label: team,
          data: commitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsDeploymentFreq.push({
          label: team,
          data: deploymentFreqData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeFailureRate.push({
          label: team,
          data: changeFailureRateData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeLeadTime.push({
          label: team,
          data: changeLeadTimeData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });
      });

      // コミット数グラフ
      renderLineChart('commitsChart', 'コミット数', sortedDates, datasetsCommits);

      // デプロイ頻度グラフ
      renderLineChart('deploymentFrequencyChart', 'デプロイ頻度', sortedDates, datasetsDeploymentFreq);

      // 変更失敗率グラフ
      renderLineChart('changeFailureRateChart', '変更失敗率 (%)', sortedDates, datasetsChangeFailureRate);

      // 変更リードタイムグラフ
      renderLineChart('changeLeadTimeChart', '変更リードタイム (h)', sortedDates, datasetsChangeLeadTime);
    }

    /**
     * 折れ線グラフを描画する関数
     * @param canvasId グラフを描画するcanvasのID
     * @param label グラフのタイトル
     * @param labels x軸のラベル（時間）
     * @param datasets Chart.jsのデータセット
     */
    function renderLineChart(canvasId, label, labels, datasets){
      let ctx = $("#" + canvasId);
      if(ctx.data('chart')){
        ctx.data('chart').destroy();
      }
      let chart = new Chart(ctx, {
        type:'line',
        data:{
          labels: labels,
          datasets: datasets
        },
        options:{
          responsive:true,
          scales: {
            x: {
              type: 'time',
              time: {
                unit: 'day',
                tooltipFormat: 'yyyy-LL-dd'
              },
              title: {
                display: true,
                text: '日付'
              }
            },
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: label
              }
            }
          },
          plugins: {
            legend: {
              display: true,
              position: 'top',
            },
            title: {
              display: true,
              text: label
            }
          }
        }
      });
      ctx.data('chart', chart);
    }

    /**
     * チームメトリクス画面用のチャート描画関数
     * @param metrics チームメトリクスデータ
     */
    function renderTeamMetricsCharts(metrics){
      // データを日付順にソート
      metrics.sort((a, b) => new Date(a.metricDate) - new Date(b.metricDate));

      // メトリクスを日付ごとにグループ化
      let metricsByDate = {};
      metrics.forEach(m => {
        let date = m.metricDate;
        if (!metricsByDate[date]) {
          metricsByDate[date] = [];
        }
        metricsByDate[date].push(m);
      });

      // ソートされた日付リスト
      let sortedDates = Object.keys(metricsByDate).sort((a, b) => new Date(a) - new Date(b));

      // チームごとのメトリクスデータを準備
      let teams = [...new Set(metrics.map(m => m.team.teamName))];
      let datasetsCommits = [];
      let datasetsStdDevCommits = [];
      let datasetsDeploymentFreq = [];
      let datasetsChangeFailureRate = [];
      let datasetsChangeLeadTime = [];

      teams.forEach((team, index) => {
        let color = getColor(index);
        let commitsData = [];
        let stdDevCommitsData = [];
        let deploymentFreqData = [];
        let changeFailureRateData = [];
        let changeLeadTimeData = [];

        sortedDates.forEach(date => {
          let teamMetrics = metricsByDate[date].find(m => m.team.teamName === team);
          if (teamMetrics) {
            commitsData.push({x: date, y: teamMetrics.commits});
            stdDevCommitsData.push({x: date, y: teamMetrics.stdDevCommits});
            deploymentFreqData.push({x: date, y: teamMetrics.deploymentFrequency});
            changeFailureRateData.push({x: date, y: teamMetrics.changeFailureRate});
            changeLeadTimeData.push({x: date, y: teamMetrics.changeLeadTime});
          } else {
            commitsData.push({x: date, y: null});
            stdDevCommitsData.push({x: date, y: null});
            deploymentFreqData.push({x: date, y: null});
            changeFailureRateData.push({x: date, y: null});
            changeLeadTimeData.push({x: date, y: null});
          }
        });

        datasetsCommits.push({
          label: team,
          data: commitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsStdDevCommits.push({
          label: team,
          data: stdDevCommitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsDeploymentFreq.push({
          label: team,
          data: deploymentFreqData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeFailureRate.push({
          label: team,
          data: changeFailureRateData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeLeadTime.push({
          label: team,
          data: changeLeadTimeData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });
      });

      // コミット数グラフ
      renderLineChart('teamMetricsChart', 'コミット数', sortedDates, datasetsCommits);

      // 標準偏差コミット数グラフ
      renderLineChart('stdDevCommitsChart', 'コミット数の標準偏差', sortedDates, datasetsStdDevCommits);

      // デプロイ頻度グラフ
      renderLineChart('teamDeploymentFrequencyChart', 'デプロイ頻度', sortedDates, datasetsDeploymentFreq);

      // 変更失敗率グラフ
      renderLineChart('teamChangeFailureRateChart', '変更失敗率 (%)', sortedDates, datasetsChangeFailureRate);

      // 変更リードタイムグラフ
      renderLineChart('teamChangeLeadTimeChart', '変更リードタイム (h)', sortedDates, datasetsChangeLeadTime);
    }

    // チームメトリクス画面に必要なチャートを追加
    // team-metrics-area 内に追加するため、index.htmlも更新済み

    // 参加者メトリクス
    function loadTeamsForParticipantMetrics(){
      showLoading();
      $.get("/api/teams", function(teams){
        let select = $("#select-team-participantmetrics").empty();
        teams.forEach(t=>{
          select.append($("<option>").val(t.teamId).text(t.teamName));
        });
        hideLoading();
      }).fail(function(){
        showToast("チーム一覧取得失敗","error");
        hideLoading();
      });
    }

    $("#btn-fetch-participant-metrics").click(function(){
      let teamId = $("#select-team-participantmetrics").val();
      if(!teamId){
        showToast("チームを選択してください","error");
        return;
      }
      showLoading();
      // チームに紐づく参加者のメトリクスを取得
      $.ajax({
        url: "/api/participant-metrics/team/" + encodeURIComponent(teamId),
        type: "GET",
        success: function(metrics){
          renderParticipantMetricsCharts(metrics);
          hideLoading();
        },
        error: function(){
          showToast("メトリクス取得に失敗しました","error");
          hideLoading();
        }
      });
    });

    function renderParticipantMetricsCharts(metrics){
      // データを日付順にソート
      metrics.sort((a, b) => new Date(a.metricDate) - new Date(b.metricDate));

      // メトリクスを日付ごとにグループ化
      let metricsByDate = {};
      metrics.forEach(m => {
        let date = m.metricDate;
        if (!metricsByDate[date]) {
          metricsByDate[date] = [];
        }
        metricsByDate[date].push(m);
      });

      // ソートされた日付リスト
      let sortedDates = Object.keys(metricsByDate).sort((a, b) => new Date(a) - new Date(b));

      // 参加者ごとのメトリクスデータを準備
      let participants = [...new Set(metrics.map(m => m.participant.participantName))];
      let datasetsCommits = [];
      let datasetsDeploymentFreq = [];
      let datasetsChangeFailureRate = [];
      let datasetsChangeLeadTime = [];

      participants.forEach((participant, index) => {
        let color = getColor(index);
        let commitsData = [];
        let deploymentFreqData = [];
        let changeFailureRateData = [];
        let changeLeadTimeData = [];

        sortedDates.forEach(date => {
          let participantMetrics = metricsByDate[date].find(m => m.participant.participantName === participant);
          if (participantMetrics) {
            commitsData.push({x: date, y: participantMetrics.commits});
            deploymentFreqData.push({x: date, y: participantMetrics.deploymentFrequency});
            changeFailureRateData.push({x: date, y: participantMetrics.changeFailureRate});
            changeLeadTimeData.push({x: date, y: participantMetrics.changeLeadTime});
          } else {
            commitsData.push({x: date, y: null});
            deploymentFreqData.push({x: date, y: null});
            changeFailureRateData.push({x: date, y: null});
            changeLeadTimeData.push({x: date, y: null});
          }
        });

        datasetsCommits.push({
          label: participant,
          data: commitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsDeploymentFreq.push({
          label: participant,
          data: deploymentFreqData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeFailureRate.push({
          label: participant,
          data: changeFailureRateData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeLeadTime.push({
          label: participant,
          data: changeLeadTimeData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });
      });

      // コミット数グラフ
      renderLineChart('participantMetricsChart', '個人コミット数', sortedDates, datasetsCommits);

      // デプロイ頻度グラフ
      renderLineChart('participantDeploymentFrequencyChart', 'デプロイ頻度', sortedDates, datasetsDeploymentFreq);

      // 変更失敗率グラフ
      renderLineChart('participantChangeFailureRateChart', '変更失敗率 (%)', sortedDates, datasetsChangeFailureRate);

      // 変更リードタイムグラフ
      renderLineChart('participantChangeLeadTimeChart', '変更リードタイム (h)', sortedDates, datasetsChangeLeadTime);
    }

    // 設定画面
    function loadBranchRules(){
      showLoading();
      $.get("/api/settings/branch-rules", function(rules){
        $("#master-regex").val(rules.masterRegex);
        $("#story-regex").val(rules.storyRegex);
        $("#issue-regex").val(rules.issueRegex);
        hideLoading();
      }).fail(function(){
        showToast("ブランチルール取得に失敗しました","error");
        hideLoading();
      });
    }

    $("#branch-rules-form").submit(function(e){
      e.preventDefault();
      let masterRegex = $("#master-regex").val().trim();
      let storyRegex = $("#story-regex").val().trim();
      let issueRegex = $("#issue-regex").val().trim();

      if(!masterRegex || !storyRegex || !issueRegex){
        showToast("全てのフィールドを入力してください","error");
        return;
      }

      showLoading();
      $.ajax({
        url: "/api/settings/branch-rules",
        type: "POST",
        contentType:"application/json",
        data:JSON.stringify({
          masterRegex: masterRegex,
          storyRegex: storyRegex,
          issueRegex: issueRegex
        }),
        success: function(){
          showToast("ブランチルールが保存されました");
          hideLoading();
        },
        error: function(){
          showToast("ブランチルール保存に失敗しました","error");
          hideLoading();
        }
      });
    });

    /**
     * 折れ線グラフを描画する関数（共通）
     * @param canvasId グラフを描画するcanvasのID
     * @param label グラフのタイトル
     * @param labels x軸のラベル（時間）
     * @param datasets Chart.jsのデータセット
     */
    function renderLineChart(canvasId, label, labels, datasets){
      let ctx = $("#" + canvasId);
      if(ctx.data('chart')){
        ctx.data('chart').destroy();
      }
      let chart = new Chart(ctx, {
        type:'line',
        data:{
          labels: labels,
          datasets: datasets
        },
        options:{
          responsive:true,
          scales: {
            x: {
              type: 'time',
              time: {
                unit: 'day',
                tooltipFormat: 'yyyy-LL-dd'
              },
              title: {
                display: true,
                text: '日付'
              }
            },
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: label
              }
            }
          },
          plugins: {
            legend: {
              display: true,
              position: 'top',
            },
            title: {
              display: true,
              text: label
            }
          }
        }
      });
      ctx.data('chart', chart);
    }

    /**
     * チームごとの色を生成する関数
     * @param index 色のインデックス
     * @return RGBA色文字列
     */
    function getColor(index){
      const colors = [
        'rgba(255, 99, 132, 1)',    // Red
        'rgba(54, 162, 235, 1)',    // Blue
        'rgba(255, 206, 86, 1)',    // Yellow
        'rgba(75, 192, 192, 1)',    // Green
        'rgba(153, 102, 255, 1)',   // Purple
        'rgba(255, 159, 64, 1)',    // Orange
        'rgba(199, 199, 199, 1)',   // Grey
        'rgba(83, 102, 255, 1)',    // Indigo
        'rgba(255, 99, 255, 1)',    // Pink
        'rgba(99, 255, 132, 1)'     // Light Green
      ];
      return colors[index % colors.length];
    }

    // イベント選択が変更された際にグラフを更新する
    $("#select-event-dashboard").change(function(){
      let selectedEventId = $(this).val();
      if(selectedEventId){
        showLoading();
        $.ajax({
          url: "/api/team-metrics/event/" + encodeURIComponent(selectedEventId),
          type: "GET",
          success: function(metrics){
            renderDashboardCharts(metrics);
            hideLoading();
          },
          error: function(){
            showToast("メトリクス取得に失敗しました","error");
            hideLoading();
          }
        });
      }
    });

    $("#btn-refresh-metrics").click(function(){
      // メトリクス更新API呼び出し
      showLoading();
      $.ajax({
        url: "/api/team-metrics/fetch-all",
        type: "POST",
        success: function(){
          showToast("メトリクスが更新されました");
          let eventId = $("#select-event-dashboard").val();
          loadDashboardMetrics(eventId);
        },
        error: function(){
          showToast("メトリクス更新に失敗しました","error");
          hideLoading();
        }
      });
    });

    // イベント管理
    function loadEvents(){
      showLoading();
      $.get("/api/events", function(events){
        initDataTable("#event-table");
        let tbody = $("#event-table-body").empty();
        events.forEach(e=>{
          let tr = $("<tr>");
          tr.append($("<td>").text(e.eventId));
          tr.append($("<td>").text(e.eventName));
          tr.append($("<td>").text(e.startDate));
          tr.append($("<td>").text(e.endDate||""));
          let delBtn = $("<button class='btn btn-sm btn-danger me-2'>削除</button>").click(function(){
            if(confirm("削除しますか？")){
              showLoading();
              $.ajax({
                url:"/api/events/"+encodeURIComponent(e.eventId),
                type:"DELETE",
                success:function(){
                  showToast("イベントを削除しました");
                  loadEvents();
                },
                error:function(){
                  showToast("イベント削除に失敗しました","error");
                  hideLoading();
                }
              });
            }
          });
          let editBtn = $("<button class='btn btn-sm btn-primary me-2'>編集</button>").click(function(){
            // 編集モーダルの表示（実装が必要）
            showToast("編集機能は未実装です","info");
          });
          tr.append($("<td>").append(editBtn).append(delBtn));
          tbody.append(tr);
        });
        $("#event-table").DataTable();
        hideLoading();
      }).fail(function(){
        showToast("イベント一覧取得に失敗しました","error");
        hideLoading();
      });
    }

    $("#btn-save-event").click(function(){
      let eventId = $("#event-id").val().trim();
      let eventName = $("#event-name").val().trim();
      let startDate = $("#event-start-date").val();
      let endDate = $("#event-end-date").val() || null;

      if(!eventId || !eventName || !startDate){
        showToast("必須項目を入力してください","error");
        return;
      }

      showLoading();
      $.ajax({
        url:"/api/events",
        type:"POST",
        contentType:"application/json",
        data:JSON.stringify({
          eventId: eventId,
          eventName: eventName,
          startDate: startDate + "T00:00:00",
          endDate: endDate ? endDate + "T23:59:59" : null
        }),
        success:function(){
          showToast("イベントが登録されました");
          $("#eventModal").modal('hide');
          loadEvents();
        },
        error:function(){
          showToast("イベント登録に失敗しました。入力を確認してください。","error");
          hideLoading();
        }
      });
    });

    // チーム管理
    function loadTeams(){
      showLoading();
      $.get("/api/teams", function(teams){
        initDataTable("#team-table");
        let tbody = $("#team-table-body").empty();
        teams.forEach(t=>{
          let tr = $("<tr>");
          tr.append($("<td>").text(t.teamId));
          tr.append($("<td>").text(t.teamName));
          tr.append($("<td>").text(t.event.eventId));
          let delBtn = $("<button class='btn btn-sm btn-danger me-2'>削除</button>").click(function(){
            if(confirm("削除しますか？")){
              showLoading();
              $.ajax({
                url:"/api/teams/"+encodeURIComponent(t.teamId),
                type:"DELETE",
                success:function(){
                  showToast("チームを削除しました");
                  loadTeams();
                },
                error:function(){
                  showToast("チーム削除に失敗しました","error");
                  hideLoading();
                }
              });
            }
          });
          let editBtn = $("<button class='btn btn-sm btn-primary me-2'>編集</button>").click(function(){
            // 編集モーダルの表示（実装が必要）
            showToast("編集機能は未実装です","info");
          });
          tr.append($("<td>").append(editBtn).append(delBtn));
          tbody.append(tr);
        });
        $("#team-table").DataTable();
        hideLoading();
      }).fail(function(){
        showToast("チーム一覧取得に失敗しました","error");
        hideLoading();
      });
    }

    $("#btn-save-team").click(function(){
      let teamName = $("#team-name-input").val().trim();
      let eventId = $("#team-event-id-input").val().trim();

      if(!teamName || !eventId){
        showToast("必須項目を入力してください","error");
        return;
      }

      showLoading();
      $.ajax({
        url:"/api/teams?eventId="+encodeURIComponent(eventId),
        type:"POST",
        contentType:"application/json",
        data: JSON.stringify({
          teamName: teamName
        }),
        success:function(){
          showToast("チームが登録されました");
          $("#teamModal").modal('hide');
          loadTeams();
        },
        error:function(){
          showToast("チーム登録に失敗しました","error");
          hideLoading();
        }
      });
    });

    // 参加者管理
    function loadParticipants(){
      showLoading();
      $.get("/api/participants", function(participants){
        initDataTable("#participant-table");
        let tbody = $("#participant-table-body").empty();
        participants.forEach(p=>{
          let tr = $("<tr>");
          tr.append($("<td>").text(p.participantId));
          tr.append($("<td>").text(p.participantName));
          tr.append($("<td>").text(p.team.teamId));
          tr.append($("<td>").text(p.participantGitlabId));
          tr.append($("<td>").text(p.participantGitlabEmail));
          let delBtn = $("<button class='btn btn-sm btn-danger me-2'>削除</button>").click(function(){
            if(confirm("削除しますか？")){
              showLoading();
              $.ajax({
                url:"/api/participants/"+encodeURIComponent(p.participantId),
                type:"DELETE",
                success:function(){
                  showToast("参加者を削除しました");
                  loadParticipants();
                },
                error:function(){
                  showToast("参加者削除に失敗しました","error");
                  hideLoading();
                }
              });
            }
          });
          let editBtn = $("<button class='btn btn-sm btn-primary me-2'>編集</button>").click(function(){
            // 編集モーダルの表示（実装が必要）
            showToast("編集機能は未実装です","info");
          });
          tr.append($("<td>").append(editBtn).append(delBtn));
          tbody.append(tr);
        });
        $("#participant-table").DataTable();
        hideLoading();
      }).fail(function(){
        showToast("参加者一覧取得に失敗しました","error");
        hideLoading();
      });
    }

    $("#btn-save-participant").click(function(){
      let name = $("#participant-name-input").val().trim();
      let gitlabId = $("#participant-gitlab-id").val().trim();
      let gitlabEmail = $("#participant-gitlab-email").val().trim();
      let teamId = $("#participant-team-id").val().trim();

      if(!name || !gitlabId || !gitlabEmail || !teamId){
        showToast("必須項目を全て入力してください","error");
        return;
      }

      showLoading();
      $.ajax({
        url:"/api/participants?teamId="+encodeURIComponent(teamId),
        type:"POST",
        contentType:"application/json",
        data:JSON.stringify({
          participantName: name,
          participantGitlabId: gitlabId,
          participantGitlabEmail: gitlabEmail
        }),
        success:function(){
          showToast("参加者が登録されました");
          $("#participantModal").modal('hide');
          loadParticipants();
        },
        error:function(){
          showToast("参加者登録に失敗しました","error");
          hideLoading();
        }
      });
    });

    // チームメトリクス
    function loadEventsForTeamMetrics(){
      showLoading();
      $.get("/api/events", function(events){
        let select = $("#select-event-teammetrics").empty();
        events.forEach(e=>{
          select.append($("<option>").val(e.eventId).text(e.eventName));
        });
        hideLoading();
      }).fail(function(){
        showToast("イベント一覧取得失敗","error");
        hideLoading();
      });
    }

    $("#btn-fetch-team-metrics").click(function(){
      let eventId = $("#select-event-teammetrics").val();
      if(!eventId){
        showToast("イベントを選択してください","error");
        return;
      }
      showLoading();
      // イベントIDに紐づくチームメトリクスを取得
      $.ajax({
        url: "/api/team-metrics/event/" + encodeURIComponent(eventId),
        type: "GET",
        success: function(metrics){
          renderTeamMetricsCharts(metrics);
          hideLoading();
        },
        error: function(){
          showToast("メトリクス取得に失敗しました","error");
          hideLoading();
        }
      });
    });

    function renderTeamMetricsCharts(metrics){
      // データを日付順にソート
      metrics.sort((a, b) => new Date(a.metricDate) - new Date(b.metricDate));

      // メトリクスを日付ごとにグループ化
      let metricsByDate = {};
      metrics.forEach(m => {
        let date = m.metricDate;
        if (!metricsByDate[date]) {
          metricsByDate[date] = [];
        }
        metricsByDate[date].push(m);
      });

      // ソートされた日付リスト
      let sortedDates = Object.keys(metricsByDate).sort((a, b) => new Date(a) - new Date(b));

      // チームごとのメトリクスデータを準備
      let teams = [...new Set(metrics.map(m => m.team.teamName))];
      let datasetsCommits = [];
      let datasetsStdDevCommits = [];
      let datasetsDeploymentFreq = [];
      let datasetsChangeFailureRate = [];
      let datasetsChangeLeadTime = [];

      teams.forEach((team, index) => {
        let color = getColor(index);
        let commitsData = [];
        let stdDevCommitsData = [];
        let deploymentFreqData = [];
        let changeFailureRateData = [];
        let changeLeadTimeData = [];

        sortedDates.forEach(date => {
          let teamMetrics = metricsByDate[date].find(m => m.team.teamName === team);
          if (teamMetrics) {
            commitsData.push({x: date, y: teamMetrics.commits});
            stdDevCommitsData.push({x: date, y: teamMetrics.stdDevCommits});
            deploymentFreqData.push({x: date, y: teamMetrics.deploymentFrequency});
            changeFailureRateData.push({x: date, y: teamMetrics.changeFailureRate});
            changeLeadTimeData.push({x: date, y: teamMetrics.changeLeadTime});
          } else {
            commitsData.push({x: date, y: null});
            stdDevCommitsData.push({x: date, y: null});
            deploymentFreqData.push({x: date, y: null});
            changeFailureRateData.push({x: date, y: null});
            changeLeadTimeData.push({x: date, y: null});
          }
        });

        datasetsCommits.push({
          label: team,
          data: commitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsStdDevCommits.push({
          label: team,
          data: stdDevCommitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsDeploymentFreq.push({
          label: team,
          data: deploymentFreqData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeFailureRate.push({
          label: team,
          data: changeFailureRateData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeLeadTime.push({
          label: team,
          data: changeLeadTimeData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });
      });

      // コミット数グラフ
      renderLineChart('teamMetricsChart', 'コミット数', sortedDates, datasetsCommits);

      // 標準偏差コミット数グラフ
      renderLineChart('stdDevCommitsChart', 'コミット数の標準偏差', sortedDates, datasetsStdDevCommits);

      // デプロイ頻度グラフ
      renderLineChart('teamDeploymentFrequencyChart', 'デプロイ頻度', sortedDates, datasetsDeploymentFreq);

      // 変更失敗率グラフ
      renderLineChart('teamChangeFailureRateChart', '変更失敗率 (%)', sortedDates, datasetsChangeFailureRate);

      // 変更リードタイムグラフ
      renderLineChart('teamChangeLeadTimeChart', '変更リードタイム (h)', sortedDates, datasetsChangeLeadTime);
    }

    // 参加者メトリクス
    function loadTeamsForParticipantMetrics(){
      showLoading();
      $.get("/api/teams", function(teams){
        let select = $("#select-team-participantmetrics").empty();
        teams.forEach(t=>{
          select.append($("<option>").val(t.teamId).text(t.teamName));
        });
        hideLoading();
      }).fail(function(){
        showToast("チーム一覧取得失敗","error");
        hideLoading();
      });
    }

    $("#btn-fetch-participant-metrics").click(function(){
      let teamId = $("#select-team-participantmetrics").val();
      if(!teamId){
        showToast("チームを選択してください","error");
        return;
      }
      showLoading();
      // チームに紐づく参加者のメトリクスを取得
      $.ajax({
        url: "/api/participant-metrics/team/" + encodeURIComponent(teamId),
        type: "GET",
        success: function(metrics){
          renderParticipantMetricsCharts(metrics);
          hideLoading();
        },
        error: function(){
          showToast("メトリクス取得に失敗しました","error");
          hideLoading();
        }
      });
    });

    function renderParticipantMetricsCharts(metrics){
      // データを日付順にソート
      metrics.sort((a, b) => new Date(a.metricDate) - new Date(b.metricDate));

      // メトリクスを日付ごとにグループ化
      let metricsByDate = {};
      metrics.forEach(m => {
        let date = m.metricDate;
        if (!metricsByDate[date]) {
          metricsByDate[date] = [];
        }
        metricsByDate[date].push(m);
      });

      // ソートされた日付リスト
      let sortedDates = Object.keys(metricsByDate).sort((a, b) => new Date(a) - new Date(b));

      // 参加者ごとのメトリクスデータを準備
      let participants = [...new Set(metrics.map(m => m.participant.participantName))];
      let datasetsCommits = [];
      let datasetsDeploymentFreq = [];
      let datasetsChangeFailureRate = [];
      let datasetsChangeLeadTime = [];

      participants.forEach((participant, index) => {
        let color = getColor(index);
        let commitsData = [];
        let deploymentFreqData = [];
        let changeFailureRateData = [];
        let changeLeadTimeData = [];

        sortedDates.forEach(date => {
          let participantMetrics = metricsByDate[date].find(m => m.participant.participantName === participant);
          if (participantMetrics) {
            commitsData.push({x: date, y: participantMetrics.commits});
            deploymentFreqData.push({x: date, y: participantMetrics.deploymentFrequency});
            changeFailureRateData.push({x: date, y: participantMetrics.changeFailureRate});
            changeLeadTimeData.push({x: date, y: participantMetrics.changeLeadTime});
          } else {
            commitsData.push({x: date, y: null});
            deploymentFreqData.push({x: date, y: null});
            changeFailureRateData.push({x: date, y: null});
            changeLeadTimeData.push({x: date, y: null});
          }
        });

        datasetsCommits.push({
          label: participant,
          data: commitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsDeploymentFreq.push({
          label: participant,
          data: deploymentFreqData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeFailureRate.push({
          label: participant,
          data: changeFailureRateData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeLeadTime.push({
          label: participant,
          data: changeLeadTimeData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });
      });

      // コミット数グラフ
      renderLineChart('participantMetricsChart', '個人コミット数', sortedDates, datasetsCommits);

      // デプロイ頻度グラフ
      renderLineChart('participantDeploymentFrequencyChart', 'デプロイ頻度', sortedDates, datasetsDeploymentFreq);

      // 変更失敗率グラフ
      renderLineChart('participantChangeFailureRateChart', '変更失敗率 (%)', sortedDates, datasetsChangeFailureRate);

      // 変更リードタイムグラフ
      renderLineChart('participantChangeLeadTimeChart', '変更リードタイム (h)', sortedDates, datasetsChangeLeadTime);
    }

    // 設定画面
    function loadBranchRules(){
      showLoading();
      $.get("/api/settings/branch-rules", function(rules){
        $("#master-regex").val(rules.masterRegex);
        $("#story-regex").val(rules.storyRegex);
        $("#issue-regex").val(rules.issueRegex);
        hideLoading();
      }).fail(function(){
        showToast("ブランチルール取得に失敗しました","error");
        hideLoading();
      });
    }

    $("#branch-rules-form").submit(function(e){
      e.preventDefault();
      let masterRegex = $("#master-regex").val().trim();
      let storyRegex = $("#story-regex").val().trim();
      let issueRegex = $("#issue-regex").val().trim();

      if(!masterRegex || !storyRegex || !issueRegex){
        showToast("全てのフィールドを入力してください","error");
        return;
      }

      showLoading();
      $.ajax({
        url: "/api/settings/branch-rules",
        type: "POST",
        contentType:"application/json",
        data:JSON.stringify({
          masterRegex: masterRegex,
          storyRegex: storyRegex,
          issueRegex: issueRegex
        }),
        success: function(){
          showToast("ブランチルールが保存されました");
          hideLoading();
        },
        error: function(){
          showToast("ブランチルール保存に失敗しました","error");
          hideLoading();
        }
      });
    });

    /**
     * 折れ線グラフを描画する関数（共通）
     * @param canvasId グラフを描画するcanvasのID
     * @param label グラフのタイトル
     * @param labels x軸のラベル（時間）
     * @param datasets Chart.jsのデータセット
     */
    function renderLineChart(canvasId, label, labels, datasets){
      let ctx = $("#" + canvasId);
      if(ctx.data('chart')){
        ctx.data('chart').destroy();
      }
      let chart = new Chart(ctx, {
        type:'line',
        data:{
          labels: labels,
          datasets: datasets
        },
        options:{
          responsive:true,
          scales: {
            x: {
              type: 'time',
              time: {
                unit: 'day',
                tooltipFormat: 'yyyy-LL-dd'
              },
              title: {
                display: true,
                text: '日付'
              }
            },
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: label
              }
            }
          },
          plugins: {
            legend: {
              display: true,
              position: 'top',
            },
            title: {
              display: true,
              text: label
            }
          }
        }
      });
      ctx.data('chart', chart);
    }

    /**
     * チームメトリクス画面用のチャート描画関数
     * @param metrics チームメトリクスデータ
     */
    function renderTeamMetricsCharts(metrics){
      // データを日付順にソート
      metrics.sort((a, b) => new Date(a.metricDate) - new Date(b.metricDate));

      // メトリクスを日付ごとにグループ化
      let metricsByDate = {};
      metrics.forEach(m => {
        let date = m.metricDate;
        if (!metricsByDate[date]) {
          metricsByDate[date] = [];
        }
        metricsByDate[date].push(m);
      });

      // ソートされた日付リスト
      let sortedDates = Object.keys(metricsByDate).sort((a, b) => new Date(a) - new Date(b));

      // チームごとのメトリクスデータを準備
      let teams = [...new Set(metrics.map(m => m.team.teamName))];
      let datasetsCommits = [];
      let datasetsStdDevCommits = [];
      let datasetsDeploymentFreq = [];
      let datasetsChangeFailureRate = [];
      let datasetsChangeLeadTime = [];

      teams.forEach((team, index) => {
        let color = getColor(index);
        let commitsData = [];
        let stdDevCommitsData = [];
        let deploymentFreqData = [];
        let changeFailureRateData = [];
        let changeLeadTimeData = [];

        sortedDates.forEach(date => {
          let teamMetrics = metricsByDate[date].find(m => m.team.teamName === team);
          if (teamMetrics) {
            commitsData.push({x: date, y: teamMetrics.commits});
            stdDevCommitsData.push({x: date, y: teamMetrics.stdDevCommits});
            deploymentFreqData.push({x: date, y: teamMetrics.deploymentFrequency});
            changeFailureRateData.push({x: date, y: teamMetrics.changeFailureRate});
            changeLeadTimeData.push({x: date, y: teamMetrics.changeLeadTime});
          } else {
            commitsData.push({x: date, y: null});
            stdDevCommitsData.push({x: date, y: null});
            deploymentFreqData.push({x: date, y: null});
            changeFailureRateData.push({x: date, y: null});
            changeLeadTimeData.push({x: date, y: null});
          }
        });

        datasetsCommits.push({
          label: team,
          data: commitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsStdDevCommits.push({
          label: team,
          data: stdDevCommitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsDeploymentFreq.push({
          label: team,
          data: deploymentFreqData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeFailureRate.push({
          label: team,
          data: changeFailureRateData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeLeadTime.push({
          label: team,
          data: changeLeadTimeData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });
      });

      // コミット数グラフ
      renderLineChart('teamMetricsChart', 'コミット数', sortedDates, datasetsCommits);

      // 標準偏差コミット数グラフ
      renderLineChart('stdDevCommitsChart', 'コミット数の標準偏差', sortedDates, datasetsStdDevCommits);

      // デプロイ頻度グラフ
      renderLineChart('teamDeploymentFrequencyChart', 'デプロイ頻度', sortedDates, datasetsDeploymentFreq);

      // 変更失敗率グラフ
      renderLineChart('teamChangeFailureRateChart', '変更失敗率 (%)', sortedDates, datasetsChangeFailureRate);

      // 変更リードタイムグラフ
      renderLineChart('teamChangeLeadTimeChart', '変更リードタイム (h)', sortedDates, datasetsChangeLeadTime);
    }

    // 参加者メトリクス
    function loadTeamsForParticipantMetrics(){
      showLoading();
      $.get("/api/teams", function(teams){
        let select = $("#select-team-participantmetrics").empty();
        teams.forEach(t=>{
          select.append($("<option>").val(t.teamId).text(t.teamName));
        });
        hideLoading();
      }).fail(function(){
        showToast("チーム一覧取得失敗","error");
        hideLoading();
      });
    }

    $("#btn-fetch-participant-metrics").click(function(){
      let teamId = $("#select-team-participantmetrics").val();
      if(!teamId){
        showToast("チームを選択してください","error");
        return;
      }
      showLoading();
      // チームに紐づく参加者のメトリクスを取得
      $.ajax({
        url: "/api/participant-metrics/team/" + encodeURIComponent(teamId),
        type: "GET",
        success: function(metrics){
          renderParticipantMetricsCharts(metrics);
          hideLoading();
        },
        error: function(){
          showToast("メトリクス取得に失敗しました","error");
          hideLoading();
        }
      });
    });

    function renderParticipantMetricsCharts(metrics){
      // データを日付順にソート
      metrics.sort((a, b) => new Date(a.metricDate) - new Date(b.metricDate));

      // メトリクスを日付ごとにグループ化
      let metricsByDate = {};
      metrics.forEach(m => {
        let date = m.metricDate;
        if (!metricsByDate[date]) {
          metricsByDate[date] = [];
        }
        metricsByDate[date].push(m);
      });

      // ソートされた日付リスト
      let sortedDates = Object.keys(metricsByDate).sort((a, b) => new Date(a) - new Date(b));

      // 参加者ごとのメトリクスデータを準備
      let participants = [...new Set(metrics.map(m => m.participant.participantName))];
      let datasetsCommits = [];
      let datasetsDeploymentFreq = [];
      let datasetsChangeFailureRate = [];
      let datasetsChangeLeadTime = [];

      participants.forEach((participant, index) => {
        let color = getColor(index);
        let commitsData = [];
        let deploymentFreqData = [];
        let changeFailureRateData = [];
        let changeLeadTimeData = [];

        sortedDates.forEach(date => {
          let participantMetrics = metricsByDate[date].find(m => m.participant.participantName === participant);
          if (participantMetrics) {
            commitsData.push({x: date, y: participantMetrics.commits});
            deploymentFreqData.push({x: date, y: participantMetrics.deploymentFrequency});
            changeFailureRateData.push({x: date, y: participantMetrics.changeFailureRate});
            changeLeadTimeData.push({x: date, y: participantMetrics.changeLeadTime});
          } else {
            commitsData.push({x: date, y: null});
            deploymentFreqData.push({x: date, y: null});
            changeFailureRateData.push({x: date, y: null});
            changeLeadTimeData.push({x: date, y: null});
          }
        });

        datasetsCommits.push({
          label: participant,
          data: commitsData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsDeploymentFreq.push({
          label: participant,
          data: deploymentFreqData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeFailureRate.push({
          label: participant,
          data: changeFailureRateData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });

        datasetsChangeLeadTime.push({
          label: participant,
          data: changeLeadTimeData,
          borderColor: color,
          backgroundColor: color,
          fill: false,
          tension: 0.1
        });
      });

      // コミット数グラフ
      renderLineChart('participantMetricsChart', '個人コミット数', sortedDates, datasetsCommits);

      // デプロイ頻度グラフ
      renderLineChart('participantDeploymentFrequencyChart', 'デプロイ頻度', sortedDates, datasetsDeploymentFreq);

      // 変更失敗率グラフ
      renderLineChart('participantChangeFailureRateChart', '変更失敗率 (%)', sortedDates, datasetsChangeFailureRate);

      // 変更リードタイムグラフ
      renderLineChart('participantChangeLeadTimeChart', '変更リードタイム (h)', sortedDates, datasetsChangeLeadTime);
    }

    // 設定画面
    function loadBranchRules(){
      showLoading();
      $.get("/api/settings/branch-rules", function(rules){
        $("#master-regex").val(rules.masterRegex);
        $("#story-regex").val(rules.storyRegex);
        $("#issue-regex").val(rules.issueRegex);
        hideLoading();
      }).fail(function(){
        showToast("ブランチルール取得に失敗しました","error");
        hideLoading();
      });
    }

    $("#branch-rules-form").submit(function(e){
      e.preventDefault();
      let masterRegex = $("#master-regex").val().trim();
      let storyRegex = $("#story-regex").val().trim();
      let issueRegex = $("#issue-regex").val().trim();

      if(!masterRegex || !storyRegex || !issueRegex){
        showToast("全てのフィールドを入力してください","error");
        return;
      }

      showLoading();
      $.ajax({
        url: "/api/settings/branch-rules",
        type: "POST",
        contentType:"application/json",
        data:JSON.stringify({
          masterRegex: masterRegex,
          storyRegex: storyRegex,
          issueRegex: issueRegex
        }),
        success: function(){
          showToast("ブランチルールが保存されました");
          hideLoading();
        },
        error: function(){
          showToast("ブランチルール保存に失敗しました","error");
          hideLoading();
        }
      });
    });

    /**
     * チームごとの色を生成する関数
     * @param index 色のインデックス
     * @return RGBA色文字列
     */
    function getColor(index){
      const colors = [
        'rgba(255, 99, 132, 1)',    // Red
        'rgba(54, 162, 235, 1)',    // Blue
        'rgba(255, 206, 86, 1)',    // Yellow
        'rgba(75, 192, 192, 1)',    // Green
        'rgba(153, 102, 255, 1)',   // Purple
        'rgba(255, 159, 64, 1)',    // Orange
        'rgba(199, 199, 199, 1)',   // Grey
        'rgba(83, 102, 255, 1)',    // Indigo
        'rgba(255, 99, 255, 1)',    // Pink
        'rgba(99, 255, 132, 1)'     // Light Green
      ];
      return colors[index % colors.length];
    }

    // 初期ロード
    loadEventsForDashboard();
});
