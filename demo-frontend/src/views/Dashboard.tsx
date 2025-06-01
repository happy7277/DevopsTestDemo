import { useEffect, useRef, useState } from 'react';
import { Alert, Card, Space } from 'antd';
import * as echarts from 'echarts';
import { dashboard, type Point } from '../api';
import { totals } from '../model.mjs';

function Chart({ title, option }: { title: string; option: echarts.EChartsOption }) {
  const host = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (!host.current) return;
    const chart = echarts.init(host.current);
    chart.setOption(option);
    const observer = new ResizeObserver(() => chart.resize());
    observer.observe(host.current);
    return () => { observer.disconnect(); chart.dispose(); };
  }, [option]);
  return <Card title={title}><div className="chart" ref={host} role="img" aria-label={title} /></Card>;
}

function date(offset: number) {
  return new Date(Date.now() + offset * 86_400_000).toISOString().slice(0, 10);
}

export function Dashboard({ token }: { token: string }) {
  const [from, setFrom] = useState(() => date(-6));
  const [to, setTo] = useState(() => date(0));
  const [points, setPoints] = useState<Point[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let current = true;
    setPoints([]);
    if (!from || !to || from > to) {
      setError('请选择有效的时间范围');
    } else {
      setError('');
      dashboard(token, from, to)
        .then((data) => { if (current) setPoints(data); })
        .catch((cause: Error) => { if (current) setError(cause.message); });
    }
    return () => { current = false; };
  }, [token, from, to]);

  const axes = { xAxis: { type: 'category' as const, data: points.map((p) => p.day) },
    yAxis: { type: 'value' as const }, tooltip: { trigger: 'axis' as const } };
  const summary = totals(points);
  return <section aria-label="数据看板">
    <Space wrap className="toolbar">
      <label>开始日期 <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
      <label>结束日期 <input type="date" value={to} onChange={(e) => setTo(e.target.value)} /></label>
    </Space>
    {error && <Alert type="error" message={error} />}
    <div className="charts">
      <Chart title="新增用户趋势" option={{ ...axes, series: [{
        type: 'line', data: points.map((p) => p.active + p.disabled),
      }] }} />
      <Chart title="每日启用与停用" option={{ ...axes, series: [
        { type: 'bar', name: '启用', data: points.map((p) => p.active) },
        { type: 'bar', name: '停用', data: points.map((p) => p.disabled) },
      ] }} />
      <Chart title="用户状态占比" option={{ tooltip: {}, series: [{ type: 'pie', stillShowZeroSum: false,
        data: [{ name: '启用', value: summary.active }, { name: '停用', value: summary.disabled }] }] }} />
    </div>
  </section>;
}
