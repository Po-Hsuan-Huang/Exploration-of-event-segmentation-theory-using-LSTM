%{
    This code is used after running Java code :
    MainforPCAweightsAnalysis.java

    LSTM 1-1-1 

    Running 1000 rnadom initial weights trails and observe the
    relations between initial weights and final performances (RMSE)

    select the weights sets of the best 100 trails and study the tendency
    of good initial weights.

    The original folder is called 'random initial weights analysis,'

    but it was changed to 'random initial weights [-0.1 0.1]'

%}
clear
restoredefaultpath
close all


range = 10; % range of the initial weights

if range == 0.1
    addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/random initial weights [-0.1 0.1]');
elseif range ==1
    addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/random initial weights [-1 1]/trial4');
elseif range ==10
    addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/random initial weights [-10 10]/trial6');
end

file_ID=fopen('weights_in.txt');  %  initial weights of 1000 trials.
weights_in = textscan(file_ID,'%f');

file_ID=fopen('err.txt');
errors = textscan(file_ID,'%f');

%cellnum = 13; % this is just for range = 0.1
cellnum = 12; % this is for all other ranges.
samplenum=1000;
W_in = reshape(weights_in{1},cellnum,samplenum);


% 
if range == 0.1
    errors = reshape(errors{1},101,samplenum);
    errors = errors(101,1:samplenum); % this one is just for err in /random initial weights [-0.1 0.1]
else
    errors = reshape(errors{1},1,samplenum);
 end


%% select the weights sets of the best 100 trails after sorting
%  and study the tendency of the distribution of good initial weights.

% reorder the errors.
[error2 ,idx] = sort(errors);
% reorder the samples by their consequential errors.
weights_in = W_in(:,idx);

close all


edges = linspace(-range,range,21);

for i = 1 %specify which wieght distribution to be plotted.
figure()

j1 = histogram(weights_in(i,1:100),edges);
% plot probability instead of the counts.
% histogram(weights_in(i,1:100),edges,'Normalization','probability'); 

sc = sprintf('weight idx : %d', i-1);
title(sc,'FontSize',20);
end

%% Plot distribution of the errors of all trials.

figure(2)

line  = 1 :1000;
sc = sprintf('error profile of all 1000 samples, range : %.1f', range);
plot(line,error2,'r');

ax2 = gca;
% ax2.YLim = [0 0.06];

ax2.YMinorTick ='on';
ylim([0,0.22]);
title(sc,'FontSize',20);
xlabel('nth trials sorted by error');
ylabel('error');



%%  Histogram of all errors
close all
figure(3)
edges = linspace(0.035,0.05,19);
% histogram(errors,edges,'Normalization','probability');
% histogram(errors,'Normalization','probability');
histogram(errors,edges);

sc = sprintf('error distribution of range %.1f', range);
title(sc,'FontSize',20);
xlabel('error');
ylabel('probability');
% semilogy(line,W_in(i,line),'.',line,error2(line),'r');


