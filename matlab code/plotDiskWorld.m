clear;
% add folder and its subfolders to the search paths.
%addpath(genpath('c:/matlab/myfiles'));
% add a folder to the seraching paths.
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/Update of DiskWorldEAR');
%% SumofErrors = load('SumofErrors,1.txt');
meanError = load('meanError,1.txt');
%%
%{
pred = load('pred.txt');
target = load('target.txt');
err = load('err.txt');
errvar = load('errvar.txt');

figure()

x = 1:1:length(target);
plot(x,target,'b',x,pred,'r');

xlabel('timestep');
ylabel('loc');

%}

figure()
x = 1:1:length(meanError);

axis tight;
semilogy(x,meanError);
h = gca;
% h.format_tick(h,'$100*{1}$',[],[],[],0,0);
title('Error')
xlabel('timestep');
ylabel('MeanError');
%{
figure()
shadedErrorBar(x,err,sqrt(errvar),'-ro',0.6);
title('Error with ErrorBar')
%}


