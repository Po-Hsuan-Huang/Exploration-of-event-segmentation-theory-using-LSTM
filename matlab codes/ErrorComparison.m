% Plot time lapse and RMSE average and vairance of 2D World
clear
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/log/May27');

file_ID=fopen('ErrorCompare.txt');
Error_header = textscan(file_ID,'%f',6,'Delimiter',' ','HeaderLines',1);
Error_data = textscan(file_ID,'%d%f%f%f%f%f%f','Delimiter',' ');

file_ID=fopen('TimeLapseCompare.txt');
Timelapse_header = textscan(file_ID,'%f',6,'Delimiter',' ','HeaderLines',1);
Timelapse_data = textscan(file_ID,'%d%f%f%f%f%f%f','Delimiter',' ');

file_ID=fopen('ErrorCompare_2.txt');
Error_header2 = textscan(file_ID,'%f',6,'Delimiter',' ','HeaderLines',1);
Error_data2 = textscan(file_ID,'%d%f%f%f%f%f%f','Delimiter',' ');

file_ID=fopen('TimeLapseCompare_2.txt');
Timelapse_header2 = textscan(file_ID,'%f',6,'Delimiter',' ','HeaderLines',1);
Timelapse_data2 = textscan(file_ID,'%d%f%f%f%f%f%f','Delimiter',' ');

file_ID=fopen('ErrorCompare_3.txt');
Error_header3 = textscan(file_ID,'%f',6,'Delimiter',' ','HeaderLines',1);
Error_data3 = textscan(file_ID,'%d%f%f%f%f%f%f','Delimiter',' ');

file_ID=fopen('TimeLapseCompare_3.txt');
Timelapse_header3 = textscan(file_ID,'%f',6,'Delimiter',' ','HeaderLines',1);
Timelapse_data3 = textscan(file_ID,'%d%f%f%f%f%f%f','Delimiter',' ');

file_ID=fopen('ErrorCompare_4.txt');
Error_header4 = textscan(file_ID,'%f',6,'Delimiter',' ','HeaderLines',1);
Error_data4 = textscan(file_ID,'%d%f%f%f%f%f%f','Delimiter',' ');

file_ID=fopen('TimeLapseCompare_4.txt');
Timelapse_header4 = textscan(file_ID,'%f',6,'Delimiter',' ','HeaderLines',1);
Timelapse_data4 = textscan(file_ID,'%d%f%f%f%f%f%f','Delimiter',' ');


meanError = zeros([1,size(Error_header{1})]);
varError = zeros([1,size(Error_header{1})]);
meanError2 = zeros([1,size(Error_header2{1})]);
varError2 = zeros([1,size(Error_header2{1})]);
meanError3 = zeros([1,size(Error_header3{1})]);
varError3 = zeros([1,size(Error_header3{1})]);
meanError4 = zeros([1,size(Error_header4{1})]);

for i=1:1:length(Error_header{1})
    meanError(i)= mean(Error_data{i+1});
    varError(i) = var(Error_data{i+1});
    meanError2(i)= mean(Error_data2{i+1});
    varError2(i) = var(Error_data2{i+1});
end
for i=1:1:length(Error_header3{1})
    meanError3(i)= mean(Error_data3{i+1});
    varError3(i) = var(Error_data3{i+1});
    
end
for i=1:1:length(Error_header4{1})
    meanError4(i)= mean(Error_data4{i+1});
    
end

figure(1)
lin = Error_header{1};
shadedErrorBar(lin,meanError,sqrt(varError),'-ro',0.6);
hold on
shadedErrorBar(lin,meanError2,sqrt(varError2),'-go',0.6);
shadedErrorBar(lin,meanError3,sqrt(varError3),'-bo',0.6);
plot(lin,meanError4,'-co');

hold off
title('meanError','FontSize',20)
xlabel('length of data sequence','FontSize',20)
ylabel('RMSE','FontSize',20)
% legend('r: v =1','g: v =0.2','b: v =0.02','c: v = 0.002')

%%
meanTimeLapse = zeros([1,size(Timelapse_header{1})]);
varTimeLapse = zeros([1,size(Timelapse_header{1})]);
meanTimeLapse2 = zeros([1,size(Timelapse_header2{1})]);
varTimeLapse2 = zeros([1,size(Timelapse_header2{1})]);
meanTimeLapse3 = zeros([1,size(Timelapse_header3{1})]);
varTimeLapse3 = zeros([1,size(Timelapse_header3{1})]);
meanTimeLapse4 = zeros([1,size(Timelapse_header4{1})]);


for i=1:1:length(Error_header{1})
    meanTimeLapse(i)= mean(Timelapse_data{i+1});
    varTimeLapse(i) = var(Timelapse_data{i+1});
    meanTimeLapse2(i)= mean(Timelapse_data2{i+1});
    varTimeLapse2(i) = var(Timelapse_data2{i+1});
end
for i=1:1:length(Error_header3{1})
    meanTimeLapse3(i)= mean(Timelapse_data3{i+1});
    varTimeLapse3(i) = var(Timelapse_data3{i+1});
    
end
for i=1:1:length(Error_header4{1})
    meanTimeLapse4(i)= mean(Timelapse_data4{i+1});
    
end

%%
figure(2)
lin = Timelapse_header{1};
shadedErrorBar(lin,meanTimeLapse,sqrt(varTimeLapse),'-ro',0.6);
hold on
shadedErrorBar(lin,meanTimeLapse2,sqrt(varTimeLapse2),'-go',0.6);
shadedErrorBar(lin,meanTimeLapse3,sqrt(varTimeLapse3),'-bo',0.6);
plot(lin,meanTimeLapse4,'-co');

hold off
title('meanTimeLapse','FontSize',20)
xlabel('length of data sequence','FontSize',20)
ylabel('runtime (ns)','FontSize',20)
% legend('r: v =1','g: v =0.2','b: v =0.02','c: v = 0.002')
%%

figure(3)
lin = Error_header{1};
plot(lin,100*sqrt(varError)./meanError,'ro-',lin,100*sqrt(varError2)./meanError2,'go-',lin,100*sqrt(varError3)./meanError3,'bo-');
title('Error Coefficient of Variation (%)','FontSize',20)
xlabel('length of sequence','FontSize',20)
ylabel('Error Coefficient of Variation (%)','FontSize',20)
legend('r: v =1','g: v =0.2','b: v =0.02','c: v = 0.002')

figure(4)
lin = Timelapse_header{1};
plot(lin,100*sqrt(varTimeLapse)./meanTimeLapse,'ro-',lin,100*sqrt(varTimeLapse2)./meanTimeLapse2,'go-',lin,100*sqrt(varTimeLapse3)./meanTimeLapse3,'bo-');
title('TimeLapse Coefficient of Variation ','FontSize',20)
xlabel('length of sequence','FontSize',20)
ylabel('Error Coefficient of Variation (%)','FontSize',20)
legend('r: v =1','g: v =0.2','b: v =0.02','c: v = 0.002')


