%% Sequence Length Impact on 1d-world

x = [1 5 10 50 100 200];
TimeLapse = [5.218784497E+09	6.661695518E+09	7.65268993E+09	1.382E+10	1.8838517835E+10 3.0306554039E10];

RMSE = [7.07106781186548E-01	1.60778819577066E-02	7.51031371822072E-03	2.62464994384772E-03	6.004788281483320E-04 3.331067041955093E-4];




figure()
 [hAx,hLine1,hLine2] = plotyy(x,RMSE,x,TimeLapse,'semilogy','plot');
title('1d LSTM')
xlabel('sequence length')
hLine1.LineStyle = '--';
hLine1.Marker= 'o';
hLine2.LineStyle = ':';
hLine2.Marker= 'o';
ylabel(hAx(1),'RMSE') % left y-axis
ylabel(hAx(2),'Time Lapse (ns)') % right y-axis
hAx(1).YTick = [1 10 100 1000 10000].*10^-4;    % change the ticks for the left y-axis
hAx(2).YTick = [1 10 20 30 40 50].*10^9;    % change the ticks for the left y-axis
hAx(1).YGrid = 'off';   % turn on the grid for the right y-axis
hAx(1).YLim=[10^-4,1];   % turn on the grid for the right y-axis







