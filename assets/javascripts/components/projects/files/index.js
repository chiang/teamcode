import modal from '../../commons/modal';

if (document.querySelector('#vue-file-actions')) {
  $(() => new Vue({
    el: document.querySelector('#vue-file-actions'),
    data() {
      const vmDataSet = document.querySelector('#vue-file-actions').dataset;

      return {
        groupPath: vmDataSet.groupPath,
        projectPath: vmDataSet.projectPath,
        path: vmDataSet.path,
        downloadUrl: vmDataSet.downloadUrl,
        fileName: vmDataSet.fileName,
        showDeleteModal: false
      };
    },
    computed: {
      commitMessage() {
        return this.fileName + ' 파일을 삭제했습니다 (웹에서 삭제됨).';
      },
      deletionUrl() {
        return `/${this.groupPath}/${this.projectPath}/files`;
      }
    },
    components: {
      'tc-modal': modal,
    },
    methods: {
      save() {
        alert(1);
        this.showDeleteModal = false;
      }
    },
    template:
    `
    <div class="file-actions hidden-xs">
      <a class="btn btn-standard" target="_blank" :href="downloadUrl">다운로드</a>
      <div class="btn-group tree-btn-group">

        <a href="#!" class="btn btn-warning" v-on:click="showDeleteModal = true">삭제</a>
      </div>
      <tc-modal :show="showDeleteModal" v-on:close="showDeleteModal = false">
        <div slot="header">
          <h3>변경 사항 커밋</h3>
        </div>
        <div slot="body">
          <p>이 변경 사항을 커밋하면 저장소에서 <mark>{{fileName}}</mark> 파일이 삭제됩니다. 나중에 이 파일을 복구하려면 커밋 이력에서 파일을 확인하시면 됩니다.</p>
          <form :action="deletionUrl" method="POST">
            <input type="hidden" name="_method" value="delete"/>
            <input type="hidden" name="path" :value="path"/>
            <div class="form-group row">
              <label for="delete-commit-message" class="col-sm-2 col-form-label">커밋 메시지</label>
              <div class="col-sm-10">
                <input type="text" name="message" class="form-control" id="delete-commit-message" :value="commitMessage" style="letter-spacing: 0.03rem;"/>
              </div>
            </div>

            <div slot="footer">
              <a href="#!" class="btn btn-standard" @click.stop="showDeleteModal = false">취소</a>
              <button class="btn btn-alert" type="submit">커밋합니다!</button>
            </div>
          </form>
        </div>
      </tc-modal>
    </div>
    `
  }));
}
