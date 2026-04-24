// API base URL: use VITE_API_URL if set, else fallback to current origin
const API_BASE_URL = import.meta.env.VITE_API_URL || window.location.origin;
import React, { useState } from 'react';
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
  const [format, setFormat] = useState('yaml');
  const [template, setTemplate] = useState('');
  const [params, setParams] = useState('');
  const [base64, setBase64] = useState('');
  const [loading, setLoading] = useState(false);

  // Connect to backend to get example params
  const handleGetExampleParams = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/enot/api/v1/example-params`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          template,
          format,
        }),
      });
      if (!response.ok) throw new Error('Network response was not ok');
      const data = await response.text();
      setParams(data);
      message.success('Example params loaded');
    } finally {
      setLoading(false);
    }
  };

  // Placeholder: Wire up backend call for serialization
  const handleSerialize = async () => {
    setLoading(true);
    try {
      // TODO: Replace with actual backend call
      setBase64(btoa(template + params));
      message.success('Serialized to base64');
    } catch (e) {
      message.error('Serialization failed');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(base64);
    message.success('Copied to clipboard');
  };

  return (
    <Layout style={{ minHeight: '100vh', background: TERMINAL_BG }}>
      <Header style={{ background: 'transparent', padding: '24px 24px 0 24px', border: 'none', boxShadow: 'none' }}>
        <Row align="middle" justify="start" style={{ gap: 16 }}>
          <Col style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Title level={3} style={{ margin: 0, color: '#ff7800', fontFamily: TERMINAL_FONT, letterSpacing: 2, textAlign: 'left' }}>eNot Terminal</Title>
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
        <Row gutter={24} style={{ width: '100%' }}>
          <Col xs={24} md={12} style={{ border: 'none' }}>
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
          </Col>
          <Col xs={24} md={12} style={{ border: 'none' }}>
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
        <Row align="middle" gutter={16} style={{ marginTop: 24 }}>
          <Col flex="auto">
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: 0, textAlign: 'left' }}>
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
              }}
            />
          </Col>
        </Row>
      </Content>
      <Footer style={{ textAlign: 'center', background: 'transparent', border: 'none', boxShadow: 'none', color: '#ff7800', fontFamily: TERMINAL_FONT, letterSpacing: 1 }}>
        <Text type="secondary">eNot Terminal © {new Date().getFullYear()} | Futuristic Terminal UI</Text>
      </Footer>
    </Layout>
  );
}

export default App;
