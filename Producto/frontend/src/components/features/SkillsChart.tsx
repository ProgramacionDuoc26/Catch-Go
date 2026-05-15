"use client";

import React, { useMemo } from 'react';
import { motion } from 'framer-motion';

interface SkillData {
  label: string;
  value: number; // 0 to 1
}

interface SkillsChartProps {
  data: SkillData[];
  size?: number;
  color?: string;
}

export const SkillsChart = ({ 
  data, 
  size = 300, 
  color = "#3B82F6" 
}: SkillsChartProps) => {
  const padding = 40;
  const radius = (size - padding * 2) / 2;
  const centerX = size / 2;
  const centerY = size / 2;

  const points = useMemo(() => {
    return data.map((d, i) => {
      const angle = (Math.PI * 2 * i) / data.length - Math.PI / 2;
      const x = centerX + radius * d.value * Math.cos(angle);
      const y = centerY + radius * d.value * Math.sin(angle);
      return { x, y, label: d.label, angle };
    });
  }, [data, radius, centerX, centerY]);

  const gridLevels = [0.2, 0.4, 0.6, 0.8, 1];

  const polygonPath = useMemo(() => {
    if (points.length === 0) return "";
    return points.map(p => `${p.x},${p.y}`).join(" ") + " " + `${points[0].x},${points[0].y}`;
  }, [points]);

  return (
    <div className="flex flex-col items-center justify-center p-4 bg-white dark:bg-slate-900 rounded-3xl shadow-xl">
      <svg width={size} height={size} className="overflow-visible">
        {/* Grid Levels */}
        {gridLevels.map((level) => {
          const levelRadius = radius * level;
          const levelPoints = data.map((_, i) => {
            const angle = (Math.PI * 2 * i) / data.length - Math.PI / 2;
            return {
              x: centerX + levelRadius * Math.cos(angle),
              y: centerY + levelRadius * Math.sin(angle)
            };
          });
          const path = levelPoints.map(p => `${p.x},${p.y}`).join(" ") + " " + `${levelPoints[0].x},${levelPoints[0].y}`;
          return (
            <polyline
              key={level}
              points={path}
              fill="none"
              stroke="currentColor"
              strokeWidth="1"
              className="text-slate-200 dark:text-slate-700"
            />
          );
        })}

        {/* Axis */}
        {points.map((p, i) => (
          <line
            key={i}
            x1={centerX}
            y1={centerY}
            x2={centerX + radius * Math.cos(p.angle)}
            y2={centerY + radius * Math.sin(p.angle)}
            stroke="currentColor"
            strokeWidth="1"
            className="text-slate-200 dark:text-slate-700"
          />
        ))}

        {/* Data Polygon */}
        <motion.polygon
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          points={polygonPath}
          fill={color}
          fillOpacity="0.2"
          stroke={color}
          strokeWidth="3"
          strokeLinejoin="round"
        />

        {/* Labels */}
        {points.map((p, i) => {
          const labelRadius = radius + 20;
          const x = centerX + labelRadius * Math.cos(p.angle);
          const y = centerY + labelRadius * Math.sin(p.angle);
          return (
            <text
              key={i}
              x={x}
              y={y}
              textAnchor="middle"
              dominantBaseline="middle"
              className="text-[10px] font-semibold fill-slate-500 dark:fill-slate-400 uppercase tracking-wider"
            >
              {p.label}
            </text>
          );
        })}
      </svg>
    </div>
  );
};
