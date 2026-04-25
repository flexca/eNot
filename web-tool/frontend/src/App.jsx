// API base URL: use VITE_API_URL if set, else fallback to current origin
const API_BASE_URL = import.meta.env.VITE_API_URL || window.location.origin;
import React, { useState, useRef } from 'react';
import { List } from 'antd';
import { parseTree, findNodeAtLocation } from 'jsonc-parser';
import { Layout, Button, Select, Typography, Row, Col, Space, message, Tooltip, Input, Divider } from 'antd';
import { SettingOutlined, CopyOutlined, ExportOutlined, BulbOutlined, FileTextOutlined } from '@ant-design/icons';
import MonacoEditor from '@monaco-editor/react';
import './App.css';

const TERMINAL_BG = '#232629';
const TERMINAL_BORDER = '0 0 0 2px #ff7800, 0 0 12px 0 #ff780055';
const TERMINAL_FONT = '"Ubuntu Mono", "Fira Mono", "Consolas", monospace';

const { Header, Content, Sider, Footer } = Layout;
const { Title, Text } = Typography;
const { Option } = Select;

function App() {
  // Error state: array of { details, jsonPointer }
  const [errors, setErrors] = useState([]);
  // Copy button handler
  const handleCopy = () => {
    if (base64) {
      navigator.clipboard.writeText(base64);
      message.success('Copied to clipboard');
    } else {
      message.info('Nothing to copy');
    }
  };
  // Serialize handler: call backend, show base64 on success, errors on failure
  const handleSerialize = async () => {
    setLoading(true);
    setErrors([]);
    setBase64('');
    try {
      const payload = { template, params, format };
      const response = await fetch(`${API_BASE_URL}/enot/api/v1/serialize`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      let text = await response.text();
      let data = null;
      let jsonErrors = null;
      try {
        data = JSON.parse(text);
      } catch {}
      if (data && Array.isArray(data.jsonErrors)) {
        jsonErrors = data.jsonErrors;
      }
      // Show error panel for syntax errors (jsonErrors present)
      if (jsonErrors && jsonErrors.length > 0) {
        setErrors(jsonErrors);
        setBase64('');
        message.error('Validation errors found');
        return;
      }
      // Show error panel for generic errors (errorType === 'generic' and errorMessage)
      if (data && data.errorType === 'generic' && data.errorMessage) {
        setErrors([{ details: data.errorMessage }]);
        setBase64('');
        message.error(data.errorMessage);
        return;
      }
      if (!response.ok) {
        message.error((data && data.errorMessage) || text || 'Failed to serialize');
        return;
      }
      // If errorMessage is present, treat as a general error
      if (data && data.errorMessage) {
        message.error(data.errorMessage);
        setBase64('');
        return;
      }
      // Success: show base64, hide errors
      setErrors([]);
      if (data && data.base64) {
        setBase64(data.base64);
        message.success('Serialization successful');
      } else {
        setBase64(text || '');
        message.success('Serialization successful');
      }
    } catch (e) {
      message.error('Failed to serialize');
      setBase64('');
    } finally {
      setLoading(false);
    }
  };
  const [format, setFormat] = useState('yaml');
  const [template, setTemplate] = useState('');
  const [params, setParams] = useState('');
  const [loading, setLoading] = useState(false);
  const [base64, setBase64] = useState('');
  const templateEditorRef = useRef(null);
  const monacoRef = useRef(null);

  // Monaco onMount handler
  const handleTemplateEditorMount = (editor, monaco) => {
    templateEditorRef.current = editor;
    monacoRef.current = monaco;
    // Restore Monaco's default JSON validation
    if (monaco.languages && monaco.languages.json && monaco.languages.json.jsonDefaults) {
      monaco.languages.json.jsonDefaults.setDiagnosticsOptions({ validate: true });
    }
  };

  // Connect to backend to get example params
  const handleGetExampleParams = async () => {
    setLoading(true);
    setErrors([]);
    try {
      const payload = { template, format };
      const response = await fetch(`${API_BASE_URL}/enot/api/v1/example-params`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      let text = await response.text();
      let data = null;
      let jsonErrors = null;
      try {
        data = JSON.parse(text);
      } catch {}
      if (data && Array.isArray(data.jsonErrors)) {
        jsonErrors = data.jsonErrors;
      }
      // Show error panel for syntax errors (jsonErrors present)
      if (jsonErrors && jsonErrors.length > 0) {
        setErrors(jsonErrors);
        setParams('');
        message.error('Validation errors found');
        return;
      }
      // Show error panel for generic errors (errorType === 'generic' and errorMessage)
      if (data && data.errorType === 'generic' && data.errorMessage) {
        setErrors([{ details: data.errorMessage }]);
        setParams('');
        message.error(data.errorMessage);
        return;
      }
      if (!response.ok) {
        message.error((data && data.errorMessage) || text || 'Failed to load example params');
        return;
      }
      if (data) {
        if (data.errorType) {
          message.error(data.errorMessage || 'Error occurred');
          return;
        }
        setParams(JSON.stringify(data, null, 2));
        message.success('Example params loaded');
      } else {
        setParams(text);
        message.success('Example params loaded');
      }
    } catch (e) {
      message.error('Failed to load example params');
    } finally {
      setLoading(false);
    }
  };

  // --- UI/JSX ---
  return (
    <>
      <Layout style={{ minHeight: '100vh', background: TERMINAL_BG }}>
        <Header style={{ background: 'transparent', padding: '24px 24px 0 24px', border: 'none', boxShadow: 'none' }}>
          <Row align="middle" justify="start" style={{ gap: 16 }}>
            <Col style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <Title level={3} style={{ margin: 0, color: '#ff7800', fontFamily: TERMINAL_FONT, letterSpacing: 2, textAlign: 'left' }}>eNot web-tool</Title>
              <Select
                value={format}
                onChange={setFormat}
                style={{ width: 120, fontFamily: TERMINAL_FONT, background: '#232629', color: '#ffb86c', border: '1.5px solid #ff7800', borderRadius: 8, boxShadow: '0 0 8px #ff780055', marginLeft: 0 }}
                dropdownStyle={{ fontFamily: TERMINAL_FONT, background: '#232629', color: '#ffb86c' }}
                popupMatchSelectWidth={false}
              >
                <Option value="yaml" style={{ background: '#232629', color: '#ffb86c' }}>YAML</Option>
                <Option value="json" style={{ background: '#232629', color: '#ffb86c' }}>JSON</Option>
              </Select>
            </Col>
          </Row>
        </Header>
        <Content style={{ padding: 24, flex: 1, background: 'transparent', maxWidth: '100vw' }}>
          <Row gutter={24} style={{ width: '100%', margin: 0 }}>
            <Col xs={24} md={12} style={{ border: 'none', paddingLeft: 0, paddingRight: 12 }}>
              <Title level={5} style={{ textAlign: 'left', marginBottom: 8, color: '#ffb86c', fontFamily: TERMINAL_FONT, letterSpacing: 1 }}>
                <FileTextOutlined /> Template ({format.toUpperCase()})
              </Title>
              <MonacoEditor
                height="300px"
                language={format}
                theme="vs-dark"
                value={template}
                onChange={setTemplate}
                options={{
                  minimap: { enabled: false },
                  fontFamily: TERMINAL_FONT,
                  fontSize: 16,
                  lineNumbers: 'on',
                  scrollbar: { vertical: 'hidden', horizontal: 'hidden' },
                  overviewRulerLanes: 0,
                  renderLineHighlight: 'none',
                  cursorStyle: 'block',
                  cursorBlinking: 'solid',
                  theme: 'vs-dark',
                }}
                className="terminal-editor"
                onMount={handleTemplateEditorMount}
              />
              <div style={{ marginTop: 12, textAlign: 'left', display: 'flex', gap: 8 }}>
                <Tooltip title="Serialize template">
                  <Button
                    icon={<ExportOutlined />}
                    onClick={handleSerialize}
                    loading={loading}
                    style={{
                      background: 'linear-gradient(90deg, #ffb86c 0%, #ff7800 100%)',
                      border: '1.5px solid #ffb86c',
                      color: '#181a1b',
                      fontFamily: TERMINAL_FONT,
                      fontWeight: 'bold',
                      letterSpacing: 1,
                      boxShadow: '0 0 8px #ffb86c88',
                      borderRadius: 8,
                      marginLeft: 0,
                      transition: 'background 0.2s, color 0.2s',
                    }}
                    onMouseOver={e => { e.currentTarget.style.background = 'linear-gradient(90deg, #ff7800 0%, #ffb86c 100%)'; e.currentTarget.style.color = '#181a1b'; }}
                    onMouseOut={e => { e.currentTarget.style.background = 'linear-gradient(90deg, #ffb86c 0%, #ff7800 100%)'; e.currentTarget.style.color = '#181a1b'; }}
                  >
                    Serialize
                  </Button>
                </Tooltip>
                <Tooltip title="Generate example params from template">
                  <Button
                    icon={<BulbOutlined />}
                    onClick={handleGetExampleParams}
                    loading={loading}
                    style={{
                      background: 'linear-gradient(90deg, #ffb86c 0%, #ff7800 100%)',
                      border: '1.5px solid #ffb86c',
                      color: '#181a1b',
                      fontFamily: TERMINAL_FONT,
                      fontWeight: 'bold',
                      letterSpacing: 1,
                      boxShadow: '0 0 8px #ffb86c88',
                      borderRadius: 8,
                      marginLeft: 0,
                      transition: 'background 0.2s, color 0.2s',
                    }}
                    onMouseOver={e => { e.currentTarget.style.background = 'linear-gradient(90deg, #ff7800 0%, #ffb86c 100%)'; e.currentTarget.style.color = '#181a1b'; }}
                    onMouseOut={e => { e.currentTarget.style.background = 'linear-gradient(90deg, #ffb86c 0%, #ff7800 100%)'; e.currentTarget.style.color = '#181a1b'; }}
                  >
                    Example Params
                  </Button>
                </Tooltip>
              </div>
              {/* Error Panel: only show if errors exist */}
              {errors.length > 0 && (
                <div style={{ marginTop: 16 }}>
                  <List
                    header={<div style={{ color: '#ff5555', fontWeight: 'bold' }}>Errors</div>}
                    bordered
                    dataSource={errors}
                    renderItem={item => (
                      <List.Item style={{ color: '#ff5555', fontFamily: TERMINAL_FONT }}>
                        {item.details}
                        {item.jsonPointer && (
                          <span style={{ color: '#888', marginLeft: 8 }}>
                            (Pointer: {item.jsonPointer})
                          </span>
                        )}
                      </List.Item>
                    )}
                    style={{ background: '#232629', border: '1.5px solid #ff5555', borderRadius: 8, marginTop: 8 }}
                  />
                </div>
              )}
            </Col>
            <Col xs={24} md={12} style={{ border: 'none', paddingLeft: 12, paddingRight: 0 }}>
              <Title level={5} style={{ textAlign: 'left', marginBottom: 8, color: '#50fa7b', fontFamily: TERMINAL_FONT, letterSpacing: 1 }}>
                <FileTextOutlined /> Params ({format.toUpperCase()})
              </Title>
              <MonacoEditor
                height="300px"
                language={format}
                theme="vs-dark"
                value={params}
                onChange={setParams}
                options={{
                  minimap: { enabled: false },
                  fontFamily: TERMINAL_FONT,
                  fontSize: 16,
                  lineNumbers: 'on',
                  scrollbar: { vertical: 'hidden', horizontal: 'hidden' },
                  overviewRulerLanes: 0,
                  renderLineHighlight: 'none',
                  cursorStyle: 'block',
                  cursorBlinking: 'solid',
                  theme: 'vs-dark',
                }}
                className="terminal-editor"
              />
            </Col>
          </Row>
          <Divider />
          {/* Base64 Output: only show if base64 is present and no errors */}
          {base64 && errors.length === 0 && (
            <Row style={{ marginTop: 24, width: '100%', margin: 0 }}>
              <Col xs={24} style={{ paddingLeft: 0, paddingRight: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: 0, textAlign: 'left', maxWidth: '100%' }}>
                  <Title level={5} style={{ margin: 0, flex: 'none', textAlign: 'left', color: '#8be9fd', fontFamily: TERMINAL_FONT, letterSpacing: 1 }}>Base64 Output</Title>
                  <Tooltip title="Copy to clipboard">
                    <Button
                      type="text"
                      icon={<CopyOutlined />}
                      onClick={handleCopy}
                      disabled={!base64}
                      style={{ marginLeft: 8, color: '#8be9fd' }}
                    />
                  </Tooltip>
                </div>
                <Input.TextArea
                  value={base64}
                  readOnly
                  autoSize={{ minRows: 2, maxRows: 4 }}
                  style={{
                    fontFamily: TERMINAL_FONT,
                    marginTop: 8,
                    background: '#181a1b',
                    color: '#8be9fd',
                    border: '1.5px solid #ff7800',
                    boxShadow: TERMINAL_BORDER,
                    borderRadius: 8,
                    padding: 12,
                    width: '100%',
                    boxSizing: 'border-box',
                    overflowX: 'auto',
                    maxWidth: '100%',
                  }}
                />
              </Col>
            </Row>
          )}
        </Content>
        <Footer style={{ textAlign: 'center', background: 'transparent', border: 'none', boxShadow: 'none', color: '#ff7800', fontFamily: TERMINAL_FONT, letterSpacing: 1 }}>
          <Text type="secondary">eNot web-tool, {new Date().getFullYear()}</Text>
        </Footer>
      </Layout>
    </>
  );
}

export default App;
