%{
    Running 100 rnadom initial weights trails and observe the
    relations between initial weights and final weights after
    1000 time steps.
%}
close all
clear
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/good weights analysis');

file_ID=fopen('weights_in.txt');
weights_in = textscan(file_ID,'%f');
file_ID=fopen('weights_out.txt');
weights_out = textscan(file_ID,'%f');
file_ID=fopen('err.txt');
errors = textscan(file_ID,'%f');
cellnum = 13;
samplenum=100;
W_in = reshape(weights_in{1},cellnum,samplenum);
W_out = reshape(weights_out{1},cellnum,samplenum);
errors = reshape(errors{1},1,samplenum);


W_in = cat(1,W_in,1:100); % adding original indices to the weight array

%% W_in analysis: The initial weights are entirely random in this task.
close all
[error2 ,idx] = sort(errors);
weights_in = W_in(:,idx);


figure(1)
line = 1:400;
edges = linspace(-0.1,0.1,21);
hold on
for i = 2
%semilogyline,weights(i,line),'.',line,error2(line),'r');

histogram(weights_in(i,1:10),edges);
sc = sprintf('weight idx : %d', i-1);
title(sc,'FontSize',20);
end

%% W_out analysis study the distribution of final weights.

% edges = linspace(-4,1,40);

figure(2)
i = 4;
histogram(W_out(i,1:100),21);
sc = sprintf('weight idx : %d', i-1);
title(sc,'FontSize',20);