%{  
    This code should be run after java code 
    Running 100 random initial weights trails and observe the
    relations between initial weights and final weights after
    1000 time steps.
%}
clear
close all
restoredefaultpath
% addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/good weights analysis');

range = 10; % range of the initial weights

if range == 0.1
    addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/random initial weights [-0.1 0.1]/trial2');
elseif range ==1
    addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/random initial weights [-1 1]/trial4');
elseif range ==10
    addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/random initial weights [-10 10]/trial6');
end

file_ID=fopen('weights_in.txt');
weights_in = textscan(file_ID,'%f');
file_ID=fopen('weights_out.txt');
weights_out = textscan(file_ID,'%f');
file_ID=fopen('err.txt');
errors = textscan(file_ID,'%f');
cellnum = 12;
samplenum=1000;
W_in = reshape(weights_in{1},cellnum,samplenum);
W_out = reshape(weights_out{1},cellnum,samplenum);

errors = reshape(errors{1},1,samplenum);
%% W_in analysis: The initial weights are entirely random in this task.

i = 8;


[weight_in_vec,idx]= sort(W_in(i,1:100));
weights_in = W_in(:,idx);
weights_out = W_out(:,idx);

legend1 =round(1000*[ min(weight_in_vec(1)),max(weight_in_vec(30))])/1000;
legend2 =round(1000*[ min(weight_in_vec(36)),max(weight_in_vec(65))])/1000;
legend3 =round(1000*[ min(weight_in_vec(71)),max(weight_in_vec(100))])/1000;
str1 = sprintf('%.3f~%.3f',legend1(1),legend1(2));
str2 = sprintf('%.3f~%.3f',legend2(1),legend2(2));
str3 = sprintf('%.3f~%.3f',legend3(1),legend3(2));

figure(1)
nbins = 10;
BinLim = [min(weights_out(i,1:100)),max(weights_out(i,1:100))];
edges = linspace(BinLim(1),BinLim(2),nbins);   

h1 = histogram(weights_out(i,1:30),edges);
sc = sprintf('weight idx : %d', i-1);
title(sc,'FontSize',20);
hold on
h2 = histogram(weights_out(i,36:65),edges);
h3 = histogram(weights_out(i,71:100),edges);
h1.Normalization = 'probability';
h2.Normalization = 'probability';
h3.Normalization = 'probability';
hold off
P = legend(str1,str2,str3,'Location','North');
P.FontSize=15;

figure(2)
ax1 = bar([h1.Values; h2.Values; h3.Values]',1);
TickLabel = cell(1,length(h1.BinEdges)-1);
for k= 1 : size(TickLabel,2)
TickLabel{k} =num2str( round((h1.BinEdges(k)+h1.BinWidth/2)*100)/100 );
end


L =legend(str1,str2,str3,'Location','North');
L.FontSize=15;
set(gca, 'XTick',1:1:size(TickLabel,2));
set(gca,'XTickLabel',TickLabel);
sc = sprintf(' OutputWeight Dist  \n input weight idx : %d', i-1);
title(sc,'FontSize',20);
xlabel('final weight');
ylabel('probability');