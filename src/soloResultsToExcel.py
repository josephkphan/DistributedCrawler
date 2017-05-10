import xlwt

book = xlwt.Workbook(encoding="utf-8")
excelSheetName = "soloResults.xls"
sheet1 = book.add_sheet("Sheet 1")

with open('crawler_results/results.txt') as f:
    lines = f.readlines()
    row = 0
    sheet1.write(row,0,'Solo Results')
    row = 2
    sheet1.write(row,0,'Reddit Subpage')
    sheet1.write(row,1,'# Subpages')
    sheet1.write(row,2,'Time(ms)')
    row = 3
    for line in lines:
        split = line.split(':')
        if split[0] != 'Total Time':
            sheet1.write(row,0,split[0])
            sheet1.write(row,1,split[1])
            sheet1.write(row,2,split[2])
            row += 1
        else:
            row = 1
            sheet1.write(row,0,'Total Time:')
            sheet1.write(row,1,split[1])


book.save(excelSheetName)