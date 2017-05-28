import json

class MyJsonHandler():
    """This class handles writing data objects to files and loading in data objects"""
    @staticmethod
    def save_data_to_json_file(data, file_path):
        """
        Store data as json in designated file_path
        """
        out_file = open(file_path, "w")
        json.dump(data, out_file, indent=4)
        out_file.close()

    @staticmethod
    def get_data_from_json_file(file_path):
        """
        get data as json in designated file_path and returns the loaded json
        """
        try:
            with open(file_path) as f:
                return json.load(f)
        except IOError as e:
            print 'could not read ' + file_path
            
count_dict = MyJsonHandler.get_data_from_json_file('crawler_results/count.json')
with open('resources/crawl_list.txt', 'r+') as myfile:
    for page in count_dict:
        myfile.write(str(page) + ':'+ str(count_dict[page]) + '\n')
    myfile.close()

    