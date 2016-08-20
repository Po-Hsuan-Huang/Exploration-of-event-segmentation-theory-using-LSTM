%% Epoch vs Error,Speed

epoch = [100 500 1000 2000 4000 8000];
w = repmat(1E-16,[1,6]); % no data

%v = 1
meanError = [5.541E-01	1.110E-05	2.328E-16	1.577E-15	4.255E-15 1.0694E-15];
varError = [7.006E-05	1.776E-10	2.328E-16	1.246E-30	3.517E-29 0];
meanTimeLapse = [2.840E+08	1.143E+09	1.985E+09	4.024E+09	1.046E+10 24393346087];
varTimeLapse = [3.471E+15	6.287E+15	1.596E+16	1.425E+17	2.041E+18 2.041E+19] ;
%v = 0.2
meanError2 = [5.196E-01	7.331E-02	5.532E-02	6.979E-02	5.255E-02	0.05392103165049633];
varError2 = [1.957E+16	7.873E+15	4.660E+16	7.212E+16	3.004E+16	9.052E+17];
meanTimeLapse2 = [2.807E+08	1.013E+09	2.032E+09	1.321E+10	1.066E+10	29069537548];
varTimeLapse2 = w;

%v = 0.002
meanError3 = [4.074E-01	4.628E-02	2.181E-02	2.229E-02	2.066E-02	 0.03497486384118675];
varError3 = w;
meanTimeLapse3 = [4.074E-01	4.628E-02	2.181E-02	2.229E-02	2.066E-02	 0.03497486384118675];
varTimeLapse3 = w;






figure(1)
shadedErrorBar(epoch,log10(meanError),sqrt(varError),'ro',0.6);
hold on
shadedErrorBar(epoch,log10(meanError2),sqrt(varError),'go',0.6);
shadedErrorBar(epoch,log10(meanError3),sqrt(varError),'co',0.6);


hold off
% title('meanTimeLapse','FontSize',20)
xlabel('duration of training ','FontSize',20)
ylabel('log(RMSE)','FontSize',20)
% legend('r: v = 1', 'c: v =0.002')

figure(2)
shadedErrorBar(epoch,meanTimeLapse,sqrt(varTimeLapse),'-ro',0.6);
hold on
shadedErrorBar(epoch,meanTimeLapse2,sqrt(varTimeLapse),'-go',0.6);
shadedErrorBar(epoch,meanTimeLapse3,sqrt(varTimeLapse3),'-co',0.6);

hold off
% title('meanTimeLapse ','FontSize',20)
xlabel('duration of training ','FontSize',20)
ylabel('runtime (ns)','FontSize',20)
% legend('r: v = 1', 'c: v =0.002')
