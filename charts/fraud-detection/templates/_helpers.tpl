{{/*
Templates for Fraud Detection System Microservices
*/}}

{{- define "fraud-detection.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "microservice.deployment" -}}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .name }}
  labels:
    {{- include "fraud-detection.labels" . | nindent 4 }}
spec:
  replicas: {{ .replicaCount | default 3 }}
  selector:
    matchLabels:
      app: {{ .name }}
  template:
    metadata:
      labels:
        app: {{ .name }}
    spec:
      containers:
      - name: {{ .name }}
        image: "{{ .image.repository }}:{{ .image.tag }}"
        imagePullPolicy: {{ .image.pullPolicy }}
        ports:
        - containerPort: {{ .containerPort }}
        env:
        {{- range .env }}
        - name: {{ .name }}
          value: "{{ .value }}"
        {{- end }}
        resources:
          {{- toYaml .resources | nindent 10 }}
        {{- if .livenessProbe.enabled }}
        livenessProbe:
          httpGet:
            path: {{ .livenessProbe.path }}
            port: {{ .containerPort }}
          initialDelaySeconds: {{ .livenessProbe.initialDelaySeconds }}
          periodSeconds: {{ .livenessProbe.periodSeconds }}
        {{- end }}
        {{- if .readinessProbe.enabled }}
        readinessProbe:
          httpGet:
            path: {{ .readinessProbe.path }}
            port: {{ .containerPort }}
          initialDelaySeconds: {{ .readinessProbe.initialDelaySeconds }}
          periodSeconds: {{ .readinessProbe.periodSeconds }}
        {{- end }}
{{- end -}}