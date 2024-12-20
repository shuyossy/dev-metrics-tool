# 要件定義書

## 概要

本システムは、新人ハッカソン研修における各チームの開発生産性を可視化・分析し、参加者やメンター、運営が状況を把握・フィードバックするための**開発生産性管理ツール**です。  

## 背景・目的

- 新人ハッカソン研修では、複数チームが約2週間でWebアプリケーションを開発する。
- 運営やメンターがGitLab上のコミット状況やテスト結果をもとにメトリクスを分析し、改善提案やフィードバックを行いたい。
- 従来はスクリプトや手動作業でメトリクスを取得していたが、チーム数増加に伴い負荷が増大。
- 本ツールをWebアプリケーション化することで、運営が簡易にメトリクス取得・分析し、参加者のモチベーション向上やメンターによる効果的なサポートを実現する。

## 対象範囲

- 新人ハッカソン研修での利用が主目的。
- 全社展開は想定していないが、将来的に他研修への展開はオプションとして考慮可能。

## 想定利用者

- **運営**：全チーム・全イベントのメトリクス収集・集計、サマリー確認、イベント切り替え操作。
- **メンター**：自分が担当する（もしくは指定された）チームやイベントのメトリクス確認とフィードバック。
- **研修参加者（新人）**：自チームや自分個人のメトリクス、フィードバック結果を参照。

## 前提条件・制約事項

- ソースコード管理はDSS GitLabを使用。
- ブランチ命名・階層ルール（Master、Story、Issueブランチ）を初期ルールとして設定画面で変更可能。
  - ルールのデフォルト設定は以下の通り
    - **Masterブランチ**
        - 命名規則：`^master$`
    - **Storyブランチ**
        - 命名規則：`^story*`
        - 生成規則：Masterブランチから切ること
    - **Issueブランチ**
        - 命名規則：`^issue*`
        - 生成規則：Storyブランチから切ること
- 外れ値処理・データ補正は行わない。
- GitLab上のユーザ名やメールは、そのまま利用する（名寄せは必須ではない）。
- メトリクスは最低1日1回収集、サーバ負荷に応じて頻度調整可能。
- 過去イベントや過去ハッカソンのメトリクスも保持し、参照可能にする。
- 複数回開催されるハッカソン（イベント）に対応できるよう、イベントIDを用いてデータ管理・表示することを可能とし、現在開催中のイベントや過去イベントのメトリクス参照、イベント同士の比較など実施可能にする。
- 認証・認可は不要。全ユーザが同一機能へアクセス可能。
- 技術スタック：Spring Boot (Java) + HTML/CSS/JavaScript、Bootstrap、jQuery、Chart.js等を用いるWebアプリケーション。

## 機能要件

### イベント管理

- 開催している、または開催予定のイベントを設定する機能を提供する。
  - イベントID、イベント名、開始日と終了日を設定することができる
    - 終了日はnullを許容
  - イベントIDは外部で発行される文字列を、ユーザがシステム上で入力可能
  - メトリクスは現在開催中のイベントに紐づくチームのみ収集する
- 過去イベントIDを指定してそのイベントのメトリクスを参照可能。

### チーム管理機能

- チーム登録・編集・削除機能。
- チーム登録時に紐づくイベント、チーム名を設定可能。
- 現在開催中イベントに紐づくチームのみ、メトリクス収集対象になる。

### 参加者管理機能

- チームごとに参加者を登録・編集・削除できる画面を提供。
- 参加者情報には以下を保持：
  - `participant_id`（連番主キー）
  - `participant_name` (参加者名)
  - `team_id`（所属チーム）
  - `participant_gitlab_id`（GitLabユーザ識別子）
  - `participant_gitlab_email`（GitLabユーザメール）
- 登録された参加者一覧から個人メトリクス表示可能。

### メトリクス収集機能

- 指定イベントIDに紐づくチームを対象に、1日1回バッチでメトリクスを収集。
- 必要に応じてオンラインで「今すぐ更新」ボタンなどから手動トリガーによる再収集も可能。
- メトリクス例：
  - コミット数
  - 個人別コミット数標準偏差（チーム内でコミットに偏りがないかの指標）
  - デプロイ頻度（Mainブランチへのマージ回数）
  - 変更失敗率（Mainブランチマージ時のテスト失敗率）
  - 変更リードタイム（Storyブランチの平均生存時間）
- ユーザ定義メトリクス設定機能あり（対象ブランチや期間をGUIで追加定義可能）。
- 個人メトリクス取得時はparticipant_gitlab_id / emailとGitLab上のコミットユーザを対応付ける。

### ブランチ戦略チェック

- ブランチ戦略ルール（Master、Story、Issueブランチの正規表現・生成元チェック）のチェックを実行し、違反時は画面上で注意喚起。
- ルールは設定画面で編集可能。

### フィードバック機能

- メトリクス基準値に応じた簡易メッセージ表示。
- 値に応じて色分け表示で視覚的フィードバック（表・アイコンなど）。
  - 色分けはヒートマップで実現。  
- PDF出力での色分け反映。

### メトリクスサマリー出力

- イベント単位でメトリクスサマリーをWeb画面上に表示し、PDFダウンロード可能。
- 過去イベントIDを指定して過去分も参照・PDF出力可能。

### イベント間比較機能

- 複数イベントIDを選択し、それらのメトリクスを比較表示。
- Web上でグラフ・表表示のみでOK（PDF不要）。
- チームDBから取得したユニークなイベントID一覧をマルチセレクトUIで選択可能。

### 基準値・ユーザ定義メトリクスの妥当性チェック

- 入力値が不正な場合はエラーメッセージ表示（Bootstrapのアラートなど使用）。
- 正規表現や数値範囲など一般的な妥当性チェックを実施。

## 非機能要件

- パフォーマンス：1日1回程度の処理で問題ない規模。大規模負荷対策は不要。
- 可用性：研修期間中の安定稼働で十分。
- セキュリティ：認証認可不要だが、内部利用想定。基本的な脆弱性対策（XSS、CSRF等）は標準的に行う。
- ログ出力・エラーハンドリングは最低限実施。

## UI要件

- Bootstrap+ jQueryでリッチなUI。
- Chart.js等を利用してグラフ表示。
- 色分けやアイコンでメトリクス状態を直感的に把握可能。
- イベントID、チーム、参加者、期間などを指定しやすいUI（ドロップダウン、モーダル入力など）。
- ユーザ体験を重視してなるべく画面切り替えがないようにSPAのように実装（AJaxを活用）

## データ保持要件

- チーム・参加者・メトリクス日次スナップショットをイベント開催期間中分保持
- イベントID単位で履歴があり、過去イベントも参照可能。

## GitLab連携

- GitLab APIを利用してコミット、ブランチ、テスト結果取得。
- トークン、GitLab URLは設定ファイルで指定。
- participant_gitlab_id / emailでコミットユーザと紐付け可能な前提。