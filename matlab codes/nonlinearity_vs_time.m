%{
 plot the relation between training time and the nonlineartity of 3 series:
 1. LSTM 1-1-1
 2. LSTM 1-2-1
 3. LSTM 1-3-1
%}

addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Matlab code');

%% % right boundary

close all
epoch = [500 1000 2000];
serie1 = [5.626  17.608 71.121];
var1 = [0.910 61.253 0.392];

serie2 = [5.558 12.410 13.469];
var2 = [0.055 391.929 0.046];




figure(1)
shadedErrorBar(epoch,serie1,sqrt(var1),'-ro',0.3);
hold on
shadedErrorBar(epoch,serie2,sqrt(var2),'-bo',0.3);
% legend('LSTM 1-1-1','LSTM 1-2-1', 'LSTM 1-3-1')
title('Right Boundary Nonlinearity','FontSize',20);
xlabel('training time','FontSize',20)
ylabel('nonlinearity','FontSize',20)

%% % left boundary

serie2 =[-5.624 -9.855 -11.312];
var2 = [0.338 0.023 1.356];

serie1 = [-7.354 -14.355 -57.929];
var1 = [0.374 0.283 17.123];


figure(2)
shadedErrorBar(epoch,serie1,sqrt(var1),'-ro',0.3);
hold on
shadedErrorBar(epoch,serie2,sqrt(var2),'-bo',0.3);
% legend('LSTM 1-1-1','LSTM 1-2-1', 'LSTM 1-3-1')
title('Left Boundary Nonlinearity','FontSize',20);
xlabel('training time','FontSize',20)
ylabel('nonlinearity','FontSize',20)