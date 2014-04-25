set yrange[0:1]
plot fname using 1:3:2:6:5:8:xticlabels(9) title seriesLabel with candlesticks whiskerbars 0.5, '' using 1:4:4:4:4:8 with candlesticks lt -1 notitle