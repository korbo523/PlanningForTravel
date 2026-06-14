package com.example.planningfortravel

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java

class ListFragment : Fragment() {

    private lateinit var db: TravelDBHelper
    private lateinit var adapter: TravelAdapter
    private lateinit var progressBar: ProgressBar
    private var sortBy = TravelDBHelper.COL_DATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(
            R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = TravelDBHelper(requireContext())
        progressBar = view.findViewById(R.id.progressBar)

        adapter = TravelAdapter(
            mutableListOf(),
            onClick = { record ->
                val intent = Intent(requireContext(), AddEditActivity::class.java)
                intent.putExtra("record_id", record.no)
                intent.putExtra("view_only", true)
                startActivity(intent)
            },
            onEdit = { record ->
                val intent = Intent(requireContext(), AddEditActivity::class.java)
                intent.putExtra("record_id", record.no)
                startActivity(intent)
            },
            onDelete = { record ->
                AlertDialog.Builder(requireContext())
                    .setTitle("삭제 확인")
                    .setMessage("'${record.place}' 기록을 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        db.delete(record.no)
                        loadData()
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        )

        view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ListFragment.adapter
        }

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(Intent(requireContext(), AddEditActivity::class.java))
        }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    // 코루틴으로 비동기 DB 로딩 (가산점)
    private fun loadData() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                db.getAll(sortBy)
            }
            progressBar.visibility = View.GONE
            adapter.updateData(records)
        }
    }

    // 옵션 메뉴 (2개 이상)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sort_date -> {
                sortBy = TravelDBHelper.COL_DATE
                loadData()
                true
            }
            R.id.menu_sort_place -> {
                sortBy = TravelDBHelper.COL_PLACE
                loadData()
                true
            }
            R.id.menu_delete_all -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("전체 삭제")
                    .setMessage("모든 여행 기록을 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        db.deleteAll()
                        loadData()
                    }
                    .setNegativeButton("취소", null)
                    .show()
                true
            }
            R.id.menu_app_info -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("앱 정보")
                    .setMessage("여행 기록 앱 v1.0\n순천향대학교 모바일프로그래밍 기말 프로젝트")
                    .setPositiveButton("확인", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}